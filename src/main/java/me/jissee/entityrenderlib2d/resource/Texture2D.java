package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

/**
 * 二维材质需要实现此接口<br/>
 * Any 2D textures need to implement this interface.
 */
public interface Texture2D {
    ResourceLocation getCurrentTextureFront();
    ResourceLocation getCurrentTextureBack();
    RenderSetting getRenderSetting();
    void startOrReset();
    void pause();
    void resume();


}
