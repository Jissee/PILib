package me.jissee.entityrenderlib2d.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import me.jissee.entityrenderlib2d.render.RenderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static me.jissee.entityrenderlib2d.EntityRenderLib2D.MODID;
import static me.jissee.entityrenderlib2d.resource.ResourceUtil.*;


/**
 * This class is experimental and in need of further development. <br/><br/>
 * Create a set of Texture2D from the video file. <br/><br/>
 * The in-game video is SILENT when playing, so register SoundEffects by yourself. <br/><br/>
 * When creating video resource, the video will be decoded to frames by ffmpeg and stored in a local cache folder.<br/><br/>
 * It is not advised to include the video file in the mod resource folder, in case the file size can be large,
 * but you still can do so and extract them with {@link ResourceUtil}.<br/><br/>
 * It is advised to distribute the video separately in additional resources.<br/><br/>
 * Please check if ffmpeg is installed in the assigned path.<br/><br/>
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
     * @param videoFile    The local video file.
     * @param name         The resource name which will ve used to register textures.
     * @param nanoInterval Interval time between two frames in nanoseconds.
     * @param needDecode   Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     * @param setting      The {@link RenderSetting} of the video.
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
     * @param videoFile  The local video file.
     * @param name       The resource name which will ve used to register textures.
     * @param fpsRate    fps of the video.
     * @param needDecode Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     * @param setting    The {@link RenderSetting} of the video.
     */
    public VideoResource(File videoFile, String name, int fpsRate, boolean needDecode, RenderSetting setting){
        this.videoFile = videoFile;
        this.name = name;
        this.fps = fpsRate;
        this.nanoInterval = (long) ((1.0 / (double)fpsRate) * 1e9);
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
            LOGGER.info("Video resources are going to be decoded.");
            LOGGER.info("Make Sure you have enough disk space and ffmpeg have been installed in the MCCache folder.");
            pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use ffmpeg to decode video file.
     * @param videoFileStatic Video file.
     * @param textureName Decoded pictures filename prefix. The files will be like "name_1.png", "name_2.png", ...
     * @param fps fps of the video file.
     * @throws IOException
     */
    public static void beginDecodeFile(File videoFileStatic, String textureName, int fps) throws IOException {
        String staticOutPath = getBasePath() + "decode" + getSEP();
        String name = videoFileStatic.getName();
        ProcessBuilder pb = new ProcessBuilder(getFfmpegPath(), "-i", videoFileStatic.getPath(), "-r", "" + fps, "-f", "image2", "\"" + staticOutPath  + textureName + "_\"%d.png");
        File outDirectory = new File(staticOutPath);
        outDirectory.mkdir();
        LOGGER.info("Video resources " + name + " are going to be decoded.");
        LOGGER.info("Make Sure you have enough disk space and ffmpeg have been installed in the MCCache folder.");
        pb.start();
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
