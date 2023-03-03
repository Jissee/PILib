package me.jissee.pilib.resource;

import me.jissee.pilib.render.RenderSetting;
import net.minecraft.resources.ResourceLocation;

/**
 * 只有单帧静止的材质时使用此类<br/>
 * Use this if there is only one static frame for entity.
 */
public class SingleTexture2D implements Texture2D{
    private final ResourceLocation front;
    private final ResourceLocation back;
    private final float scaleX;
    private final float scaleY;
    private RenderSetting setting;

    public SingleTexture2D(ResourceLocation front,float scaleX,float scaleY, RenderSetting setting){
        this(front,null,scaleX,scaleY,setting);
    }
    public SingleTexture2D(ResourceLocation front,ResourceLocation back, float scaleX,float scaleY, RenderSetting setting){
        this.front = front;
        this.back = back;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
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
    public float getScaleX() {
        return scaleX;
    }

    @Override
    public float getScaleY() {
        return scaleY;
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
