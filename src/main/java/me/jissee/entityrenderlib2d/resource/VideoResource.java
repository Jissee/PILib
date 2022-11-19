package me.jissee.entityrenderlib2d.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static me.jissee.entityrenderlib2d.resource.ResourceUtil.*;


/**
 * This class is experimental and in need of further development. <br/><br/>
 * When creating video resource, the video will be decoded to frame by ffmpeg and stored in a local cache folder.<br/><br/>
 * Please check if ffmpeg is installed in the assigned path.<br/><br/>
 * It is not advised to use video resource because the size of local cache can be large.<br/><br/>
 * Please check the size of the decoded frames.<br/>
 */
public class VideoResource implements Texture2D {
    private static Logger LOGGER = LogUtils.getLogger();
    private final File videoFile;
    private final String name;
    private boolean finishedRead = false;
    private NativeImage lastImage;
    private long baseNanoTime = -1;
    private long thisNanoTime = 0;
    private long pauseNanoTime = 0;
    private int pauseIndex = 0;
    private final long nanoInterval;
    private final int fps;
    private ResourceLocation previousTexture;
    private Texture2DManager.ControlCode statusCode;
    private String outPath;
    private ProcessBuilder pb;

    /**
     * @param videoFile The local video file.
     * @param name The resource name which will ve used to register textures.
     * @param nanoInterval Interval time between two frames in nanoseconds.
     * @param needDecode Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     */
    public VideoResource(File videoFile, String name, long nanoInterval, boolean needDecode){
        this.videoFile = videoFile;
        this.name = name;
        this.nanoInterval = nanoInterval;
        this.fps = (int) (1.0 / (nanoInterval / 1e9));
        this.statusCode = Texture2DManager.ControlCode.NONE;
        if(needDecode){
            try {
                prepareDecode();
                beginDecode();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @param videoFile The local video file.
     * @param name The resource name which will ve used to register textures.
     * @param fpsRate fps of the video.
     * @param needDecode Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     */
    public VideoResource(File videoFile, String name, int fpsRate, boolean needDecode){
        this.videoFile = videoFile;
        this.name = name;
        this.fps = fpsRate;
        this.nanoInterval = (long) ((1.0 / (double)fpsRate) * 1e9);
        this.statusCode = Texture2DManager.ControlCode.NONE;
        if(needDecode){
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
    public ResourceLocation getCurrentTexture(){
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

    private int getIndex() {
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
            return pauseIndex;
        }
        thisNanoTime = System.nanoTime();
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
