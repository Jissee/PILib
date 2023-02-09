package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

public interface Texture2D {
    ResourceLocation getCurrentTextureFront();
    ResourceLocation getCurrentTextureBack();
    RenderSetting getRenderSetting();
    void startOrReset();
    void pause();
    void resume();


}
