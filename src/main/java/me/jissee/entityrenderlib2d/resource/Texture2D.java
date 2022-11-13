package me.jissee.entityrenderlib2d.resource;

import net.minecraft.resources.ResourceLocation;

public interface Texture2D {
    ResourceLocation getCurrentTexture();
    void startOrReset();
    void pause();
    void resume();


}
