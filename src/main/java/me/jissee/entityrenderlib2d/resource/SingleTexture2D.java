package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

/**
 * 只有单个静止的材质时使用此类<br/>
 * Use this if there is only one static texture for entity.
 */
public class SingleTexture2D implements Texture2D{
    private final ResourceLocation front;
    private final ResourceLocation back;
    private RenderSetting setting;

    public SingleTexture2D(ResourceLocation front, RenderSetting setting){
        this(front,null,setting);
    }
    public SingleTexture2D(ResourceLocation front,ResourceLocation back, RenderSetting setting){
        this.front = front;
        this.back = back;
        this.setting = setting;
    }
    @Override
    public ResourceLocation getCurrentTextureFront() {
        return front;
    }

    @Override
    public ResourceLocation getCurrentTextureBack() {
        if(back == null) return front;
        return back;
    }

    public void setRenderSetting(RenderSetting setting) {
        this.setting = setting;
    }
    @Override
    public RenderSetting getRenderSetting() {
        return setting;
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
