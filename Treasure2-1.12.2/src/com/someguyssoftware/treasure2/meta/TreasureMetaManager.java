/**
 * 
 */
package com.someguyssoftware.treasure2.meta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.someguyssoftware.gottschcore.GottschCore;
import com.someguyssoftware.gottschcore.enums.IRarity;
import com.someguyssoftware.gottschcore.json.JSMin;
import com.someguyssoftware.gottschcore.meta.IMeta;
import com.someguyssoftware.gottschcore.meta.IMetaArchetype;
import com.someguyssoftware.gottschcore.meta.IMetaTheme;
import com.someguyssoftware.gottschcore.meta.IMetaType;
import com.someguyssoftware.gottschcore.meta.MetaManager;
import com.someguyssoftware.gottschcore.mod.IMod;
import com.someguyssoftware.treasure2.Treasure;
import com.someguyssoftware.treasure2.enums.Rarity;

import net.minecraft.util.ResourceLocation;

/**
 * @author Mark Gottschling on Jul 29, 2019
 *
 */
public class TreasureMetaManager extends MetaManager {
	private static List<String> FOLDER_LOCATIONS = ImmutableList.of(
			"structures"
			);
	
	public TreasureMetaManager(IMod mod, String resourceFolder) {
		super(mod, resourceFolder);
		
        // build and expose template/structure folders
        buildAndExpose(getBaseResourceFolder(), Treasure.MODID, FOLDER_LOCATIONS);
	}

	// TODO is this really necesary? only need meta files loaded into master map and get by key.
	/**
	 * Call in WorldEvent.Load() event handler.
	 * Loads and registers meta files from the file system.
	 * @param modID
	 */
	public void register(String modID) {
		for (String location : FOLDER_LOCATIONS) {
		Treasure.logger.debug("registering meta files from location -> {}", location);
		// get loot table files as ResourceLocations from the file system location
		List<ResourceLocation> locs = getResourceLocations(modID, location);
		
		// load each ResourceLocation as LootTable and map it.
		for (ResourceLocation loc : locs) {
			Path path = Paths.get(loc.getResourcePath());
			if (Treasure.logger.isDebugEnabled()) {
				Treasure.logger.debug("path to meta resource loc -> {}", path.toString());
			}
			
			// load template
			Treasure.logger.debug("attempted to load custom meta file  with key -> {}", loc.toString());
			IMeta meta = load(loc);
			// add the id to the map
			if (meta == null) {
				// TODO  message
				Treasure.logger.debug("Unable to locate meta file -> {}", loc.toString());
				continue;
			}
			Treasure.logger.debug("loaded custom meta file  with key -> {} : {}", loc.toString());
			
			// TODO look for IMeta in MetaManager by file name ie x.nbt or treasure2:structures/treasure2/surface/x.nbt
			// TODO map according to meta archetype, type
			
		}	
	}
	}
	
	/**
	 * TODO how to change so that any meta file can be loaded without a new manager
	 * reads a template from an inputstream
	 */
	@Override
	protected void readFromStream(String id, InputStream stream) throws IOException, Exception {
		Treasure.logger.debug("reading meta file from stream.");
		IMeta meta = null;
		
		// read json sheet in and minify it
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JSMin minifier = new JSMin(stream, out);
		minifier.jsmin();

		// out minified json into a json reader
		ByteArrayInputStream in  = new ByteArrayInputStream(out.toByteArray());
		Reader reader = new InputStreamReader(in);
		JsonReader jsonReader = new JsonReader(reader);
		
		// create a gson builder
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		/*
		 * create types for all the properties of a StyleSheet
		 */
		Type metaArchetype = new TypeToken<List<IMetaArchetype>>() {}.getType();
//		Type metaType = new TypeToken<IMetaType>() {}.getType();
		Type metaTheme = new TypeToken<List<IMetaTheme>>() {}.getType();
		Type rarity = new TypeToken<List<IRarity>>() {}.getType();
		
		/*
		 * register the types with the custom deserializer
		 */
		gsonBuilder.registerTypeAdapter(metaArchetype, new MetaArchetypeDeserializer());
		gsonBuilder.registerTypeAdapter(IMetaType.class, new MetaTypeDeserializer());
		gsonBuilder.registerTypeAdapter(metaTheme, new MetaThemeDeserializer());
		gsonBuilder.registerTypeAdapter(rarity, new RarityDeserializer());
		Gson gson = gsonBuilder.create();	

		// read minified json into gson and generate objects
		try {
			meta = gson.fromJson(jsonReader, StructureMeta.class);
			Treasure.logger.debug("meta -> {}", meta);
		}
		catch(JsonIOException | JsonSyntaxException e) {
			// TODO change to custom exception
			throw new Exception("Unable to load meta file:", e);
		}
		finally {
			// close objects
			try {
				jsonReader.close();
			} catch (IOException e) {
				GottschCore.logger.warn("Unable to close JSON Reader when reading meta file.");
			}
		}
		
		// add meta to map
		this.getMetaMap().put(id, meta);
	}
	
	public static class MetaArchetypeDeserializer implements JsonDeserializer<List<StructureArchetype>> {
		@Override
		public List<StructureArchetype> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return context.deserialize(json, StructureArchetype.class);
		}
	}
	public static class MetaTypeDeserializer implements JsonDeserializer<StructureType> {
		@Override
		public StructureType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return context.deserialize(json, StructureType.class);
		}
	}
	public static class MetaThemeDeserializer implements JsonDeserializer<List<StructureTheme>> {
		@Override
		public List<StructureTheme> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return context.deserialize(json, StructureTheme.class);
		}
	}
	public static class RarityDeserializer implements JsonDeserializer<List<Rarity>> {
		@Override
		public List<Rarity> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return context.deserialize(json, Rarity.class);
		}
	}
}