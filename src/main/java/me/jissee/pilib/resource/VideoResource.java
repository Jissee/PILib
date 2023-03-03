package me.jissee.pilib.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import me.jissee.pilib.render.RenderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static me.jissee.pilib.PILib.MODID;
import static me.jissee.pilib.resource.ResourceUtil.*;


/**
 * 视频资源<br/>
 * 这个类是实验性的，还需要被进一步开发<br/>
 * 从视频文件中创建一个二维材质<br/>
 * 你需要自己注册一个声音时间{@link SoundEvent}，并添加到视频资源中<br/>
 * 创建视频资源后，视频会被ffmpeg解码成帧，然后缓存在本地文件夹里<br/>
 * 不建议在你的mod文件中包含视频文件，因为这可能会导致文件容量太大，但{@link ResourceUtil}也可以为你解压文件。<br/>
 * 建议发布mod时将其以额外资源的形式单独发布。<br/>
 * 你需要自己分发ffmpeg，并检查其是否在指定的文件夹里<br/>
 * 请注意帧解码后的文件大小<br/><br/>
 *
 * This class is experimental and in need of further development. <br/>
 * Create a set of Texture2D from the video file. <br/>
 * You need to register a {@link SoundEvent} by yourself and add it to the video resource. <br/>
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
    private static TextureManager tm;
    private final File videoFile;
    private SoundEvent sound;
    private SoundInstance si;
    private Entity entity;
    private final String name;
    private boolean finishedRead = false;
    private NativeImage lastImage;
    private long baseNanoTime = -1;
    private long pauseNanoTime = 0;
    private int pauseIndex = 0;
    private final long nanoInterval;
    private final double fps;
    private final float scaleX;
    private final float scaleY;
    private ResourceLocation previousTexture;
    private Texture2DManager.ControlCode statusCode;
    private String outPath;
    private RenderSetting setting;


    /**
     * @param videoFile  本地视频文件 The local video file.
     * @param name       在MC材质管理器中注册使用的名称 The resource name which will ve used to register textures.
     * @param fps        每秒帧数 Frames per second
     * @param needDecode 是否需要在创建时立即解码， 如果之前已经解码或决定之后再解码，请设为false Whether the file needs to be decoded after creation. Set to false if you have decoded before, or you want to decode later.
     * @param setting    渲染设置 The {@link RenderSetting} of the video.
     */
    public VideoResource(File videoFile, String name, double fps, SoundEvent sound, float scaleX, float scaleY, boolean needDecode, RenderSetting setting){
        this.videoFile = videoFile;
        this.name = name;
        this.fps = fps;
        this.nanoInterval = toNanoInterval(fps);
        this.sound = sound;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.setting = setting;
        this.statusCode = Texture2DManager.ControlCode.NONE;
        prepareDecode();
        if(needDecode){
            beginDecode();
        }

    }



    private NativeImage getNativeImage() {
        if(finishedRead){
            return lastImage;
        }
        int index = getIndex();
        NativeImage img;
        File textureFile = new File(outPath + name + "_" + index + ".png");
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
    @Override
    public ResourceLocation getCurrentTextureFront(){
        if(tm == null){
            tm = Minecraft.getInstance().textureManager;
        }
        if(statusCode == Texture2DManager.ControlCode.PAUSE){
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
            previousTexture = tm.register(name, new DynamicTexture(img));
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

    @Override
    public float getScaleX() {
        return scaleX;
    }

    @Override
    public float getScaleY() {
        return scaleY;
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
    public void prepareDecode(){
        outPath = getBasePath() + "decode" + getSEP();
        File outDirectory = new File(outPath);
        outDirectory.mkdir();
    }



    public void beginDecode(){
        if(this.videoFile == null){
            return;
        }
        Thread thread = new Thread(()->{
            try {
                if(!videoFile.exists()){
                    throw new IOException("Video file " + videoFile.getPath() + " does not exists");
                }
                ProcessBuilder pb = new ProcessBuilder(getFfmpegPath(), "-i", videoFile.getPath(), "-r", "" + fps, "-f", "image2", "\"" + outPath + name + "_\"%d.png");
                LOGGER.info("Video resource" + this.name + " is going to be decoded.");
                LOGGER.info("Make Sure you have enough disk space and ffmpeg have been installed in the MCCache folder.");
                pb.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }
    public VideoResource setEntity(Entity entity){
        this.entity = entity;
        return this;
    }
    public VideoResource setSound(SoundEvent sound){
        this.sound = sound;
        return this;
    }

    private void playServerSound(ServerLevel lvl, Player pPlayer, Entity pEntity, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, long pSeed){
        net.minecraftforge.event.PlayLevelSoundEvent.AtEntity event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pEntity, pSound, pCategory, pVolume, pPitch);
        if (event.isCanceled() || event.getSound() == null) return;
        pSound = event.getSound();
        pCategory = event.getSource();
        pVolume = event.getNewVolume();
        pPitch = event.getNewPitch();
        lvl.getServer().getPlayerList().broadcast(pPlayer, pEntity.getX(), pEntity.getY(), pEntity.getZ(), (double)pSound.getRange(pVolume), lvl.dimension(), new ClientboundSoundEntityPacket(pSound, pCategory, pEntity, pVolume, pPitch, pSeed));
    }
    private void playClientSound(Player pPlayer, Entity pEntity, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, long pSeed){
        net.minecraftforge.event.PlayLevelSoundEvent.AtEntity event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pEntity, pSound, pCategory, pVolume, pPitch);
        if (event.isCanceled() || event.getSound() == null) return;
        pSound = event.getSound();
        pCategory = event.getSource();
        pVolume = event.getNewVolume();
        pPitch = event.getNewPitch();
        if (pPlayer == Minecraft.getInstance().player) {
            si = new EntityBoundSoundInstance(pSound, pCategory, pVolume, pPitch, pEntity, pSeed);
            Minecraft.getInstance().getSoundManager().play(si);
        }

    }
    public SoundInstance getSoundInstance() {
        return si;
    }


    @Override
    public void startOrReset() {
        this.statusCode = Texture2DManager.ControlCode.START_OR_RESET;
        baseNanoTime = -1;
        finishedRead = false;
        if(this.entity != null){
            Level lvl = entity.level;
            if(sound != null){
                if(lvl instanceof ServerLevel slvl){
                    playServerSound(slvl,null,entity, sound,SoundSource.RECORDS,1.0f,1.0f,RandomSource.create().nextLong());
                }else{
                    playClientSound(Minecraft.getInstance().player, entity, sound,SoundSource.RECORDS,1.0f,1.0f,RandomSource.create().nextLong());

                }
            }
        }
    }

    @Override
    public void pause() {
        this.pauseNanoTime = System.nanoTime();
        this.pauseIndex = getIndex();
        this.statusCode = Texture2DManager.ControlCode.PAUSE;
        Minecraft.getInstance().getSoundManager().pause();
    }

    @Override
    public void resume() {
        if(this.statusCode == Texture2DManager.ControlCode.PAUSE){
            long diff = System.nanoTime() - pauseNanoTime;
            baseNanoTime += diff;
            this.statusCode = Texture2DManager.ControlCode.RESUME;
            Minecraft.getInstance().getSoundManager().resume();
        }
    }



}
