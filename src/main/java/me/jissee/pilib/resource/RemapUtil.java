package me.jissee.pilib.resource;

import java.util.HashMap;

public class RemapUtil {
    private static final boolean DEBUG = false;
    private static final HashMap<String, String> mapping = new HashMap<>();
    static{
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
        mapping.put("tickingSounds"    , "f_120228_");
    }
    public static String getMappedName(String mojmap){
        if(DEBUG){
            return mojmap;
        }else{
            return mapping.get(mojmap);
        }

    }

}
