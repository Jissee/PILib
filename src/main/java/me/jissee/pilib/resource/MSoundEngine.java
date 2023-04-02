package me.jissee.pilib.resource;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import static me.jissee.pilib.resource.RemapUtil.getMappedName;
import static net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField;
import static net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod;

public class MSoundEngine {
    private static SoundEngine vanillaSoundEngine;
    private static SoundBufferLibrary soundBuffers;
    private static ChannelAccess channelAccess;
    private static Listener listener;
    private static Map<SoundInstance, Integer> soundDeleteTime;
    private static Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;
    private static Multimap<SoundSource, SoundInstance> instanceBySource;
    private static Method m_calculateVolume;
    private static Field f_tickCount;
    private static final HashMap<VideoResource, SoundInstance> videoSounds = new HashMap<>();
    private MSoundEngine(){}
    public static void init(SoundEngine soundEngine){
        vanillaSoundEngine = soundEngine;
        refresh();
    }
    public static void refresh(){
        try {
            Field f_loaded = SoundEngine.class.getDeclaredField(getMappedName("loaded"));
            f_loaded.setAccessible(true);
            boolean loaded = (boolean) f_loaded.get(vanillaSoundEngine);
            f_loaded.setAccessible(false);
            if(!loaded){
                return;
            }

            Field f_soundBuffers = findField(SoundEngine.class, getMappedName("soundBuffers"));
            f_soundBuffers.setAccessible(true);
            soundBuffers = (SoundBufferLibrary) f_soundBuffers.get(vanillaSoundEngine);
            f_soundBuffers.setAccessible(false);

            Field f_channelAccess = findField(SoundEngine.class, getMappedName("channelAccess"));
            f_channelAccess.setAccessible(true);
            channelAccess = (ChannelAccess) f_channelAccess.get(vanillaSoundEngine);
            f_channelAccess.setAccessible(false);

            Field f_listener = findField(SoundEngine.class, getMappedName("listener"));
            f_listener.setAccessible(true);
            listener = (Listener) f_listener.get(vanillaSoundEngine);
            f_listener.setAccessible(false);

            Field f_soundDeleteTime = findField(SoundEngine.class, getMappedName("soundDeleteTime"));
            f_soundDeleteTime.setAccessible(true);
            soundDeleteTime = (Map<SoundInstance, Integer>) f_soundDeleteTime.get(vanillaSoundEngine);
            f_soundDeleteTime.setAccessible(false);

            Field f_instanceToChannel = findField(SoundEngine.class, getMappedName("instanceToChannel"));
            f_instanceToChannel.setAccessible(true);
            instanceToChannel = (Map<SoundInstance, ChannelAccess.ChannelHandle>) f_instanceToChannel.get(vanillaSoundEngine);
            f_instanceToChannel.setAccessible(false);

            Field f_instanceBySource = findField(SoundEngine.class, getMappedName("instanceBySource"));
            f_instanceBySource.setAccessible(true);
            instanceBySource = (Multimap<SoundSource, SoundInstance>) f_instanceBySource.get(vanillaSoundEngine);
            f_instanceBySource.setAccessible(false);

            f_tickCount = findField(SoundEngine.class, getMappedName("tickCount"));
            f_tickCount.setAccessible(true);

            m_calculateVolume = findMethod(SoundEngine.class,getMappedName("calculateVolume"), float.class, SoundSource.class);
            m_calculateVolume.setAccessible(true);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Cannot find fields. Please make sure you used the correct mapping");
        }
    }

    private static float SoundEngine_calculatePitch(SoundInstance pSound) {
        return Mth.clamp(pSound.getPitch(), 0.5F, 2.0F);
    }
    public static void stopVideoSound(VideoResource videoResource){
        SoundInstance si = videoSounds.get(videoResource);
        if(si != null){
            vanillaSoundEngine.stop(si);
        }
    }

    public static void playWithProgressOffset(VideoResource video, Vec3 pos, int progress){
        SoundEvent se = video.getSound();
        SoundInstance si = new SimpleSoundInstance(
                se, SoundSource.RECORDS, 1.0f, 1.0f,
                RandomSource.create(), pos.x, pos.y, pos.z
        );
        Minecraft.getInstance().getSoundManager().stop();
        MSoundEngine.playWithProgressOffset(si, video.getTotalTime(), progress);
        videoSounds.put(video,si);
    }

    private static void playWithProgressOffset(SoundInstance si, long totalNanoTime, int progress){
        refresh();
        //vanillaSoundEngine.stopAll();
        try {
            if(si != null && si.canPlaySound()){
                synchronized (vanillaSoundEngine){
                    WeighedSoundEvents wse = si.resolve(vanillaSoundEngine.soundManager);
                    if(wse != null){
                        Sound sound = si.getSound();
                        if(sound != SoundManager.EMPTY_SOUND){

                            float vol = si.getVolume();
                            float dstAut = Math.max(vol, 1.0F) * (float)sound.getAttenuationDistance();
                            SoundSource soundsource = si.getSource();
                            float srcDstVol = (float) m_calculateVolume.invoke(vanillaSoundEngine, vol, soundsource);
                            float pitch = SoundEngine_calculatePitch(si);
                            SoundInstance.Attenuation attenuation = si.getAttenuation();

                            Vec3 pos = new Vec3(si.getX(), si.getY(), si.getZ());

                            if (listener.getGain() > 0.0F) {
                                CompletableFuture<ChannelAccess.ChannelHandle> completablefuture = channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);
                                ChannelAccess.ChannelHandle channelaccess$channelhandle = completablefuture.join();
                                int tickCount = f_tickCount.getInt(vanillaSoundEngine);
                                soundDeleteTime.put(si, tickCount + 20);
                                instanceToChannel.put(si, channelaccess$channelhandle);
                                instanceBySource.put(soundsource, si);

                                channelaccess$channelhandle.execute((channel) -> {
                                    channel.setPitch(pitch);
                                    channel.setVolume(srcDstVol);
                                    if (attenuation == SoundInstance.Attenuation.LINEAR) {
                                        channel.linearAttenuation(dstAut);
                                    } else {
                                        channel.disableAttenuation();
                                    }
                                    channel.setLooping(false);
                                    channel.setSelfPosition(pos);
                                    channel.setRelative(false);

                                    if(sound.shouldStream()){
                                        throw new IllegalStateException("Please do not set the video sound to stream.");
                                    }
                                    soundBuffers.getCompleteBuffer(sound.getPath()).thenAccept((soundBuffer) -> {
                                        channelaccess$channelhandle.execute((channel1) -> {
                                            double nanoSecOffset = (double)progress / (double)Texture2D.MAX_PROGRESS * totalNanoTime;
                                            Channel_attachStaticBuffer_withProgressOffset(channel1, soundBuffer, (float) (nanoSecOffset / 1e9));
                                            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.sound.PlaySoundSourceEvent(vanillaSoundEngine,si, channel1));
                                        });
                                    });
                                });
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    private static void Channel_attachStaticBuffer_withProgressOffset(Channel obj, SoundBuffer buffer, float SecOffset){
        try {
            OptionalInt ret;
            int source;

            Method m_getAlBuffer = findMethod(SoundBuffer.class,getMappedName("getAlBuffer"));
            m_getAlBuffer.setAccessible(true);
            ret = (OptionalInt) m_getAlBuffer.invoke(buffer);
            m_getAlBuffer.setAccessible(false);

            Field f_source = findField(Channel.class,getMappedName("source"));
            f_source.setAccessible(true);
            source = f_source.getInt(obj);
            f_source.setAccessible(false);

            ret.ifPresent((p_83676_) -> {
                AL10.alSourcei(source, AL10.AL_BUFFER, p_83676_);
            });
            obj.play();
            AL11.alSourcef(source,AL11.AL_SEC_OFFSET, SecOffset);

        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

