package me.jissee.pilib.resource;

import me.jissee.pilib.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Range;

/**
 * 二维材质需要实现此接口<br/>
 * Any 2D textures need to implement this interface.
 */
public interface Texture2D {
    int MAX_PROGRESS = 10_0000_0000;
    /**
     * 获取当前的正面材质
     */
    ResourceLocation getCurrentTextureFront();
    /**
     * 获取当前的背面材质
     */
    ResourceLocation getCurrentTextureBack();
    /**
     * 获取材质设置
     */
    TextureSetting getTextureSetting();
    /**
     * 获取渲染设置
     */
    RenderSetting getRenderSetting();
    /**
     * 获取当前状态
     */
    TextureControlCode getStatusCode();
    /**
     * 设置新状态
     */
    void setStatusCode(TextureControlCode code);
    /**
     * 获取当前播放进度，范围从0（表示未开始播放）到10亿（表示已经到达尾部）
     */
    @Range(from = 0,to = MAX_PROGRESS)
    int getProgress();
    /**
     * 设置当前材质播放进度
     */
    void setProgress(@Range(from = 0,to = MAX_PROGRESS) int progress);
    /**
     * 暂停播放
     */
    void pause();
    /**
     * 恢复或开始播放
     */
    void resume();
    /**
     * 在没有渲染的情况下保持材质动画的播放
     * Keep texture playing while not rendering
     */
    void tick();
}
