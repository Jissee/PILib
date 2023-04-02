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
    private TextureSetting textureSetting;
    private RenderSetting renderSetting;

    public SingleTexture2D(ResourceLocation front, TextureSetting textureSetting, RenderSetting renderSetting){
        this(front,null,textureSetting, renderSetting);
    }
    public SingleTexture2D(ResourceLocation front, ResourceLocation back, TextureSetting textureSetting, RenderSetting renderSetting){
        this.front = front;
        this.back = back;
        this.textureSetting = textureSetting;
        this.renderSetting = renderSetting;
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
    public SingleTexture2D setRenderSetting(RenderSetting setting) {
        this.renderSetting = setting;
        return this;
    }
    @Override
    public RenderSetting getRenderSetting() {
        return renderSetting;
    }

    @Override
    public TextureControlCode getStatusCode() {
        return TextureControlCode.PLAYING;
    }

    @Override
    public void setStatusCode(TextureControlCode code) {}

    @Override
    public int getProgress() {return (int) (0.5 * MAX_PROGRESS);}

    @Override
    public void setProgress(int progress) {}


    public SingleTexture2D setTextureSetting(TextureSetting setting) {
        this.textureSetting = setting;
        return this;
    }
    @Override
    public TextureSetting getTextureSetting() {return textureSetting;}


    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
