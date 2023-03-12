package me.jissee.pilib.resource;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Listener;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField;
import static net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod;

public class RemapUtil {
    private static final boolean DEBUG = true;
    private static final HashMap<String, String> mapping = new HashMap<>();
    public static void init(){
        mapping.put("loaded"           , "f_120219_");
        mapping.put("soundBuffers"     , "f_120222_");
        mapping.put("channelAccess"    , "f_120224_");
        mapping.put("listener"         , "f_120221_");
        mapping.put("soundDeleteTime"  , "f_120230_");
        mapping.put("instanceToChannel", "f_120226_");
        mapping.put("instanceBySource" , "f_120227_");
        mapping.put("tickCount"        , "f_120225_");
        mapping.put("calculateVolume"  , "m_235257_");
        mapping.put("getAlBuffer"      , "m_83800_");
        mapping.put("source"           , "f_83642_");
    }

    public static String getMappedName(String mojmap){
        if(DEBUG){
            return mojmap;
        }else{
            return mapping.get(mojmap);
        }

    }

}
