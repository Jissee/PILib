package me.jissee.entityrenderlib2d.resource;

import net.minecraft.resources.ResourceLocation;

/** Use this if there is only one static texture for entity.
 *
 */
public class SingleTexture2D implements Texture2D{
    private ResourceLocation resourceLocation;
    public SingleTexture2D(ResourceLocation location){
        resourceLocation = location;
    }
    @Override
    public ResourceLocation getCurrentTexture() {
        return resourceLocation;
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
