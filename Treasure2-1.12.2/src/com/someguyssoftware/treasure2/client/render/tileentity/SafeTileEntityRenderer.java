/**
 * 
 */
package com.someguyssoftware.treasure2.client.render.tileentity;

import com.someguyssoftware.treasure2.client.model.ITreasureChestModel;
import com.someguyssoftware.treasure2.client.model.SafeModel;
import com.someguyssoftware.treasure2.tileentity.AbstractTreasureChestTileEntity;
import com.someguyssoftware.treasure2.tileentity.SafeTileEntity;

import net.minecraft.client.renderer.GlStateManager;

/**
 * 
 * @author Mark Gottschling on Feb 19, 2018
 *
 */
public class SafeTileEntityRenderer extends TreasureChestTileEntityRenderer {

 
	 /**
	  * 
	  * @param texture
	  */
	 public SafeTileEntityRenderer(String texture, ITreasureChestModel model) {
		 super(texture, model);
	 }
	 
	/**
	 * 
	 * @param te
	 * @param x
	 * @param y
	 * @param z
	 * @param partialTicks
	 * @param destroyStage
	 * @param alpha
	 */
	 @Override
    public void render(AbstractTreasureChestTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    	
    	if (!(te instanceof SafeTileEntity)) return; // should never happen

    	// add the destory textures
        if (destroyStage >= 0) {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }
    	
    	// get the model    	
    	SafeModel model = (SafeModel) getModel();   
    	
    	// bind the texture
        this.bindTexture(getTexture());
        // get the chest rotation
    	int meta = 0;
        if (te.hasWorld()) {
        	meta = te.getBlockMetadata();
        }
        int rotation = getRotation(meta);        
        
        // start render matrix
        GlStateManager.pushMatrix();
        // initial position (centered moved up)
        // (chest entity were created in Techne and need different translations than vanilla tile entities)
        GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
        
		// This rotation part is very important! Without it, your model will render upside-down.
        // (rotate 180 degrees around the z-axis)
        GlStateManager.rotate(180F, 0F, 0F, 1.0F);
        // rotate block to the correct direction that it is facing.
        GlStateManager.rotate((float)rotation, 0.0F, 1.0F, 0.0F);
        
        // if chest has locks, don't set lidAngle.
        // TODO if you add locks via chest dialog, then it won't close because it has chests
        // need another check if angle is at closed position
//        if (!te.hasLocks()) {
        	SafeTileEntity ste = (SafeTileEntity) te;
//            float latchRotation = ste.prevLatchAngle + (ste.latchAngle - ste.prevLatchAngle) * partialTicks;
//            latchRotation = 1.0F - latchRotation;
//            latchRotation = 1.0F - latchRotation * latchRotation * latchRotation;
//            model.getLatch1().rotateAngleX = -(latchRotation * (float)Math.PI / 2.0F);
        	
//        	float latchSlide = ste.prevLatchPos + (ste.latchPos - ste.prevLatchPos) * partialTicks;
//            latchSlide = 1.0F - latchSlide;
//            model.latch1.rotationPointX += latchSlide;
            
            if (ste.isLidClosed) {
	            float handleRotation = ste.prevHandleAngle + (ste.handleAngle - ste.prevHandleAngle) * partialTicks;
	            handleRotation = 1.0F - handleRotation;
	            handleRotation = 1.0F - handleRotation * handleRotation * handleRotation;
	            model.handleA1.rotateAngleZ = (handleRotation * (float)Math.PI / 2.0F);
            }
            else {
            	// render handleB rotating around y-axis
            }
            float lidRotation = ste.prevLidAngle + (ste.lidAngle - ste.prevLidAngle) * partialTicks;
            lidRotation = 1.0F - lidRotation;
            lidRotation = 1.0F - lidRotation * lidRotation * lidRotation;
            model.getLid().rotateAngleY = (lidRotation * (float)Math.PI / 2.0F);
//        } 
        
        model.renderAll(te);

        GlStateManager.popMatrix();
        // end of rendering chest entity ////
        
        
        // pop the destroy stage matrix
        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
        
        ////////////// render the locks //////////////////////////////////////
        if (!te.getLockStates().isEmpty()) {
        	renderLocks(te, x, y, z);
        }
        ////////////// end of render the locks //////////////////////////////////////
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
	
    /**
     * 
     * @param meta
     * @return
     */
//    public int getRotation(int meta) {        
//        switch(meta) {
//        case NORTH: return 0;
//        case SOUTH: return 180;
//        case WEST: return -90;
//        case EAST: return 90;
//        }
//        return 0;
//    }
    
//    /**
//     * 
//     * @param te
//     * @param x
//     * @param y
//     * @param z
//     */
//    public void renderLocks(AbstractTreasureChestTileEntity te, double x, double y, double z) {
////    	Treasure.logger.debug("=====================================================================");
//        // render locks
//        for (LockState lockState : te.getLockStates()) {
////        	Treasure.logger.debug("Render LS:" + lockState);
//        	if (lockState.getLock() != null) {        		
//        		// convert lock to an item stack
//        		ItemStack lockStack = new ItemStack(lockState.getLock());
//        		
//                GlStateManager.pushMatrix();                
//                // NOTE when rotating the item to match the face of chest, must adjust the amount of offset to the x,z axises and 
//                // not rotate() the item - rotate() just spins it in place, not around the axis of the block
//    	        GlStateManager.translate(
//    	        		(float)x + lockState.getSlot().getXOffset(),
//    	        		(float)y + lockState.getSlot().getYOffset(),
//    	        		(float)z + lockState.getSlot().getZOffset());
//    	        GlStateManager.rotate(lockState.getSlot().getRotation(), 0F, 1.0F, 0.0F);
//    	        GlStateManager.scale(0.5F, 0.5F, 0.5F);
//	        	Minecraft.getMinecraft().getRenderItem().renderItem(lockStack, ItemCameraTransforms.TransformType.NONE);
//	        	GlStateManager.popMatrix();
//        	}
//        }
//    }
   
    
//	/**
//	 * @return the texture
//	 */
//	public ResourceLocation getTexture() {
//		return texture;
//	}
//	/**
//	 * @param texture the texture to set
//	 */
//	public void setTexture(ResourceLocation texture) {
//		this.texture = texture;
//	}
//
//	/**
//	 * @return the model
//	 */
//	public ITreasureChestModel getModel() {
//		return model;
//	}
//
//	/**
//	 * @param model the model to set
//	 */
//	public void setModel(ITreasureChestModel model) {
//		this.model = model;
//	}
 
}
