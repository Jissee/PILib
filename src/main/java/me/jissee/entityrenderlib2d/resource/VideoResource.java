package me.jissee.entityrenderlib2d.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import me.jissee.entityrenderlib2d.render.RenderSetting;
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

import static me.jissee.entityrenderlib2d.EntityRenderLib2D.MODID;
import static me.jissee.entityrenderlib2d.resource.ResourceUtil.*;


/**
 * 视频资源<br/>
 * 这个类是实验性的，还需要被进一步开发<br/>
 * 从视频文件中创建一个二维材质<br/>
 * 游戏内播放的视频是无声的，使用你需要自己注册声音效果{@link SoundEvent}<br/>
 * 创建视频资源后，视频会被ffmpeg解码成帧，然后缓存在本地文件夹里<br/>
 * 不建议在你的mod文件中包含视频文件，因为这可能会导致文件容量太大，但{@link ResourceUtil}也可以为你解压文件。<br/>
 * 建议发布mod时将其以额外资源的形式单独发布。<br/>
 * 你需要自己分发ffmpeg，并检查其是否在指定的文件夹里<br/>
 * 请注意帧解码后的文件大小<br/><br/>
 *
 * This class is experimental and in need of further development. <br/>
 * Create a set of Texture2D from the video file. <br/>
 * The in-game video is SILENT when playing, so register {@link SoundEvent} by yourself. <br/>
 * When creating video resource, the video will be decoded to frames by ffmpeg and stored in a local cache folder.<br/>
 * It is not advised to include the video file in the mod resource folder, in case the file size can be large,
 * but you still can do so and extract them with {@link ResourceUtil}.<br/>
 * It is advised to distribute the video separately in additional resources.<br/>
 * You need to distribute and install ffmpeg by yourself.<br/>
 * Please check if ffmpeg is installed in the assigned path.<br/>
 * Please pay attention to the SIZE of decoded frames.<br/>
 */
public class VideoResource implements Texture2D {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation BLACK = new ResourceLocation(MODID,"textures/entity/black.png");
    private final File videoFile;
    private final String name;
    private boolean finishedRead = false;
    private NativeImage lastImage;
    private long baseNanoTime = -1;
    private long pauseNanoTime = 0;
    private int pauseIndex = 0;
    private final long nanoInterval;
    private final int fps;
    private ResourceLocation previousTexture;
    private Texture2DManager.ControlCode statusCode;
    private String outPath;
    private ProcessBuilder pb;
    private final boolean needDecode;
    private RenderSetting setting;


    /**
     * @param videoFile    本地视频文件 The local video file.
     * @param name         在MC材质管理器中注册使用的名称 The resource name which will ve used to register textures.
     * @param nanoInterval 每两帧之间间隔的时间，用纳秒表示，例如 2_000 （即2毫秒） The interval between two frames in nanoseconds.For example, 2_000 (2 milliseconds)
     * @param needDecode   是否需要在创建时立即解码， 如果之前已经解码或决定之后再解码，请设为false Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     * @param setting      渲染设置 The {@link RenderSetting} of the video.
     */
    public VideoResource(File videoFile, String name, long nanoInterval, boolean needDecode, RenderSetting setting){
        this.videoFile = videoFile;
        this.name = name;
        this.nanoInterval = nanoInterval;
        this.fps = (int) (1.0 / (nanoInterval / 1e9));
        this.setting = setting;
        this.statusCode = Texture2DManager.ControlCode.NONE;
        this.needDecode = needDecode;
        if(this.needDecode){
            try {
                prepareDecode();
                beginDecode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @param videoFile  本地视频文件 The local video file.
     * @param name       在MC材质管理器中注册使用的名称 The resource name which will ve used to register textures.
     * @param fps        每秒帧数 Frames per second
     * @param needDecode 是否需要在创建时立即解码， 如果之前已经解码或决定之后再解码，请设为false Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     * @param setting    渲染设置 The {@link RenderSetting} of the video.
     */
    public VideoResource(File videoFile, String name, int fps, boolean needDecode, RenderSetting setting){
        this.videoFile = videoFile;
        this.name = name;
        this.fps = fps;
        this.nanoInterval = (long) ((1.0 / (double)fps) * 1e9);
        this.setting = setting;
        this.statusCode = Texture2DManager.ControlCode.NONE;
        this.needDecode = needDecode;
        if(this.needDecode){
            try {
                prepareDecode();
                beginDecode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }



    private NativeImage getNativeImage() {
        if(finishedRead){
            return lastImage;
        }
        int index = getIndex();
        NativeImage img;
        //String outPath = getBasePath() + "decode" + getSEP() + name;
        File textureFile = new File(outPath + "_" + index + ".png");
        if(!textureFile.exists()){
            finishedRead = true;
            return lastImage;
        }
        try {
            InputStream istream = new FileInputStream(textureFile);
            img = NativeImage.read(istream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lastImage = img;
        return lastImage;
    }
//MC-257522
    public ResourceLocation getCurrentTextureFront(){
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return previousTexture;
        }
        NativeImage img = getNativeImage();
        if(previousTexture != null && !finishedRead){
            if(textureManager.getTexture(previousTexture) instanceof DynamicTexture dyn){
                dyn.close();
            }
            textureManager.release(previousTexture);
        }

        if(!finishedRead) {
            previousTexture = textureManager.register(name, new DynamicTexture(img));
        }

        return previousTexture;
    }

    @Override
    public ResourceLocation getCurrentTextureBack() {
        return BLACK;
    }

    public void setRenderSetting(RenderSetting setting) {
        this.setting = setting;
    }

    @Override
    public RenderSetting getRenderSetting() {
        return setting;
    }


    private int getIndex() {
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return pauseIndex;
        }
        long thisNanoTime = System.nanoTime();
        if(baseNanoTime == -1){
            baseNanoTime = thisNanoTime;
        }

        return (int) ((thisNanoTime - baseNanoTime) / nanoInterval) + 1;
    }

    /**
     * 必须在{@link #beginDecode()}之前调用<br/>
     * Must call this before {@link #beginDecode()}.
     */
    public void prepareDecode() throws IOException {
        outPath = getBasePath() + "decode" + getSEP();
        pb = new ProcessBuilder(getFfmpegPath(), "-i", videoFile.getPath(), "-r", "" + fps, "-f", "image2", "\"" + outPath  + name + "_\"%d.png");
        File outDirectory = new File(outPath);
        outDirectory.mkdir();
    }



    public void beginDecode(){
        try {
            LOGGER.info("Video resource" + this.name + " is going to be decoded.");
            LOGGER.info("Make Sure you have enough disk space and ffmpeg have been installed in the MCCache folder.");
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void startOrReset() {
        this.statusCode = Texture2DManager.ControlCode.START_OR_RESET;
        baseNanoTime = -1;
        finishedRead = false;
    }

    @Override
    public void pause() {
        this.pauseNanoTime = System.nanoTime();
        this.pauseIndex = getIndex();
        this.statusCode = Texture2DManager.ControlCode.PAUSE;
    }

    @Override
    public void resume() {
        long diff = System.nanoTime() - pauseNanoTime;
        baseNanoTime += diff;
        this.statusCode = Texture2DManager.ControlCode.RESUME;
    }


}
