package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.TexturePosition;
import net.minecraft.resources.ResourceLocation;

/** Use this if there is only one static texture for entity.
 *
 */
public class SingleTexture2D implements Texture2D{
    private ResourceLocation resourceLocation;
    private TexturePosition centeredOn;
    private boolean perpendicular;
    public SingleTexture2D(ResourceLocation location, TexturePosition position, boolean perpendicular){
        this.resourceLocation = location;
        this.centeredOn = position;
        this.perpendicular = perpendicular;
    }
    @Override
    public ResourceLocation getCurrentTexture() {
        return resourceLocation;
    }

    @Override
    public TexturePosition getCenteredOn() {
        return centeredOn;
    }

    private void setPerpendicular(boolean perpendicular){
        this.perpendicular = perpendicular;
    }
    @Override
    public boolean isPerpendicular() {
        return perpendicular;
    }

    @Override
    public void startOrReset() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
