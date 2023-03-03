package me.jissee.pilib.resource;

import me.jissee.pilib.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

/**
 * 二维材质需要实现此接口<br/>
 * Any 2D textures need to implement this interface.
 */
public interface Texture2D {
    /**
     * 获取当前的正面材质
     */
    ResourceLocation getCurrentTextureFront();
    /**
     * 获取当前的背面材质
     */
    ResourceLocation getCurrentTextureBack();
    /**
     * 获取渲染设置
     */
    RenderSetting getRenderSetting();
    /**
     * 获取材质的X尺寸（单位：方块）
     */
    float getScaleX();
    /**
     * 获取材质的Y尺寸（单位：方块）
     */
    float getScaleY();
    /**
     * 开始播放或重置
     */
    void startOrReset();
    /**
     * 暂停播放
     */
    void pause();
    /**
     * 恢复播放
     */
    void resume();


}
