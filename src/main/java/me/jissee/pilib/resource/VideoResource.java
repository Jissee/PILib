package me.jissee.pilib.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import me.jissee.pilib.render.RenderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static me.jissee.pilib.PILib.MODID;
import static me.jissee.pilib.resource.LocalResourceUtil.toNanoInterval;


/**
 * 视频资源<br/>
 * 这个类是实验性的，还需要被进一步开发<br/>
 * 从视频文件中创建一个二维材质<br/>
 * 你需要自己注册一个声音事件{@link SoundEvent}，并添加到视频资源中<br/>
 * 创建视频资源后，视频会被ffmpeg解码成帧，然后缓存在本地文件夹里<br/>
 * 不建议在你的mod文件中包含视频文件，因为这可能会导致文件容量太大，但{@link LocalResourceUtil}也可以为你解压文件。<br/>
 * 建议发布mod时将其以额外资源的形式单独发布。<br/>
 * 你需要自己分发ffmpeg，并检查其是否在指定的文件夹里<br/>
 * 请注意帧解码后的文件大小<br/><br/>
 *
 * This class is experimental and in need of further development. <br/>
 * Create a set of Texture2D from the video file. <br/>
 * You need to register a {@link SoundEvent} by yourself and add it to the video resource. <br/>
 * When creating video resource, the video will be decoded to frames by ffmpeg and stored in a local cache folder.<br/>
 * It is not advised to include the video file in the mod resource folder, in case the file size can be large,
 * but you still can do so and extract them with {@link LocalResourceUtil}.<br/>
 * It is advised to distribute the video separately in additional resources.<br/>
 * You need to distribute and install ffmpeg by yourself.<br/>
 * Please check if ffmpeg is installed in the assigned path.<br/>
 * Please pay attention to the SIZE of decoded frames.<br/>
 */
public class VideoResource implements Texture2D {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation BLACK = new ResourceLocation(MODID,"textures/entity/black.png");
    private static TextureManager tm;
    private final LocalVideoFile videoFile;
    private SoundEvent sound;
    private final long nanoInterval;
    private final long totalTime;
    private boolean finishedRead = false;
    private NativeImage lastImage;
    private long baseNanoTime = -1;
    private int pauseProgress;
    private ResourceLocation previousTexture;
    private TextureControlCode statusCode;
    private TextureSetting textureSetting;
    private RenderSetting renderSetting;
    /**
     * 由于视频解码需要一定的时间，如果在游戏内播放时未完成解码会导致错误，建议在模组加载时便定义、解码，然后再在实体注册时绑定到实体上。<br/>
     * It costs some time to decode the video and the game may crash
     * on join if the video decoding is not finished.
     * It is recommended to create and decode video resources at the mod loading phase
     * and then bind it to entities when registering them.
     * @param videoFile      本地视频文件,如果不需要使mod调用ffmpeg解码，则可空 The local video file. If there is no need for the mod to decode it by ffmpeg, it is nullable.
     * @param sound          视频的声音事件，可能在模组初始化时候声音时间没有注册完成，可以先留空，在实体内注册时添加声音 The {@link SoundEvent} of the video. It may not finish registration while the mod is initializing. So you can set this to null first and add the sound when register the video in entities.
     * @param textureSetting 材质设置 The {@link TextureSetting} of this video.
     * @param renderSetting  渲染设置 The {@link RenderSetting} of this video.
     */
    public VideoResource(LocalVideoFile videoFile, SoundEvent sound, TextureControlCode defaultStatus,
                         TextureSetting textureSetting, RenderSetting renderSetting){

        this.videoFile = videoFile;
        this.nanoInterval = toNanoInterval(videoFile.fps());
        this.totalTime = videoFile.frames() * nanoInterval;
        this.sound = sound;
        this.statusCode = defaultStatus;
        this.textureSetting = textureSetting;
        this.renderSetting = renderSetting;
    }

    private synchronized int getIndex() {
        if(statusCode == TextureControlCode.PAUSE){
            return (getProgress() / MAX_PROGRESS * videoFile.frames()) + 1;
        }
        long thisNanoTime = System.nanoTime();
        if(baseNanoTime == -1){
            baseNanoTime = thisNanoTime;
        }
        long diff = thisNanoTime - baseNanoTime;
        int index = (int) (diff / nanoInterval) + 1;
        if(index > videoFile.frames()){
            index = videoFile.frames();
        }
        return index;
    }

    private NativeImage getNativeImage() {
        if(finishedRead){
            return lastImage;
        }
        int index = getIndex();
        NativeImage img;
        File textureFile = videoFile.getImageFile(index);
        if(!textureFile.exists()){
            finishedRead = true;
            return lastImage;
        }
        try {
            InputStream istream = new FileInputStream(textureFile);
            img = NativeImage.read(NativeImage.Format.RGBA,istream);
        } catch (IOException e) {
            return lastImage;
        }
        lastImage = img;
        return lastImage;
    }

    //MC-257522
    @Override
    public ResourceLocation getCurrentTextureFront(){
        if(tm == null){
            tm = Minecraft.getInstance().textureManager;
        }
        if(statusCode == TextureControlCode.PAUSE && previousTexture != null){
            return previousTexture;
        }
        NativeImage img = getNativeImage();
        if(previousTexture != null && !finishedRead){
            if(tm.getTexture(previousTexture) instanceof DynamicTexture dyn){
                dyn.close();
            }
            tm.release(previousTexture);
        }

        if(!finishedRead) {
            previousTexture = tm.register(videoFile.name(), new DynamicTexture(img));
        }

        return previousTexture;
    }
    @Override
    public ResourceLocation getCurrentTextureBack() {
        return BLACK;
    }

    public VideoResource setRenderSetting(RenderSetting setting) {
        this.renderSetting = setting;
        return this;
    }
    @Override
    public RenderSetting getRenderSetting() {
        return renderSetting;
    }

    @Override
    public TextureControlCode getStatusCode() {
        return statusCode;
    }

    @Override
    public void setStatusCode(TextureControlCode code) {
        this.statusCode = code;
    }
    @Override
    public int getProgress() {
        if(statusCode == TextureControlCode.PAUSE){
            return pauseProgress;
        }else{
            long diff = System.nanoTime() - baseNanoTime;
            int progress = (int)((double)diff / (double)totalTime * MAX_PROGRESS);
            return Math.min(progress, MAX_PROGRESS);
        }
    }

    @Override
    public void setProgress(int progress) {
        if(progress > MAX_PROGRESS) progress = MAX_PROGRESS;
        baseNanoTime = System.nanoTime();
        baseNanoTime -= totalTime * ((double)progress / (double)MAX_PROGRESS);
        pauseProgress = progress;
    }

    public VideoResource setTextureSetting(TextureSetting setting) {
        this.textureSetting = setting;
        return this;
    }
    @Override
    public TextureSetting getTextureSetting() {
        return textureSetting;
    }

    public VideoResource setSound(SoundEvent sound){
        this.sound = sound;
        return this;
    }
    public long getTotalTime(){
        return totalTime;
    }
    public SoundEvent getSound(){
        return sound;
    }

    @Override
    public void pause() {
        this.pauseProgress = getProgress();
        this.statusCode = TextureControlCode.PAUSE;
    }
    @Override
    public void resume() {
        if(this.statusCode == TextureControlCode.PAUSE){
            setProgress(pauseProgress);
            this.statusCode = TextureControlCode.PLAYING;
        }
    }

    @Override
    public void tick() {
        getIndex();
    }
}
