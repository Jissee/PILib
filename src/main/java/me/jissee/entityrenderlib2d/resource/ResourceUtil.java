package me.jissee.entityrenderlib2d.resource;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ResourceUtil {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ArrayList<String> extractingResources = new ArrayList<>();
    private static final ArrayList<String> removeExclusion = new ArrayList<>();
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String SEP = File.separator;
    private static String basePathWin = "D:" + SEP + "MCCache" + SEP;
    private static String basePathMac = SEP + "Users" + SEP + "MCCache" + SEP;
    private static boolean needRemoveCache;


    public static void register(ResourceLocation location){
        extractingResources.add("assets/" + location.getNamespace() + "/" + location.getPath());
    }

    private static void extract(int index){
        InputStream ins = ResourceUtil.class.getClassLoader().getResourceAsStream(extractingResources.get(index));
        String fPath;

        if(OS_NAME.startsWith("win")){
            fPath = (basePathWin + extractingResources.get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
        }else{
            fPath = (basePathMac + extractingResources.get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
        }


        String dPath = "";
        for(int i = fPath.length() - 1; i >= 0; i--){
            if(fPath.charAt(i) == '\\'){
                dPath = fPath.substring(0, i);
                break;
            }
        }
        File outDir = new File(dPath);
        File outFile = new File(fPath);

        if(outFile.exists()){
            outFile.delete();
        }

        try {
            if(!outDir.exists()){
                outDir.mkdirs();
            }
            outFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(ins.readAllBytes());
            ins.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Extracted file: " + fPath);
    }

    public static void extractAll()  {
        for(int i = 0; i < extractingResources.size(); i++){
            extract(i);
        }
    }

    private static void removeRecursively(String path){
        File fl = new File(path);
        File[] files = fl.listFiles();
        for(int i = 0; i < files.length; i++){
            if (files[i].isDirectory()){
                removeRecursively(files[i].getAbsolutePath());
            }
            for(String exclusion : removeExclusion){
                if(files[i].getName().endsWith(exclusion)){
                    break;
                }
                files[i].delete();
            }

        }
    }

    public static void addExclusion(String exc) {
        removeExclusion.add(exc);
    }

    public static void removeAll(){
        removeRecursively(basePathWin);
        LOGGER.info("Old file removed!");
    }

    public static String getFileName(int index){
        String fPath;
        if(OS_NAME.startsWith("win")){
            fPath = (basePathWin + extractingResources.get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
        }else{
            fPath = (basePathMac + extractingResources.get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
        }
        return fPath;
    }

    public static String getBasePath(){
        if(OS_NAME.startsWith("win")){
            return basePathWin;
        }else{
            return basePathMac;
        }
    }

    public static String getFfmpegPath() throws IOException {
        String path;
        if(OS_NAME.startsWith("win")){
            path = getBasePath() + "ffmpeg.exe";
        }else{
            path = getBasePath() + "ffmpeg";
        }
        if(!new File(path).exists()){
            throw new IOException("ffmpeg does not exist");
        }
        return path;
    }

    public static void setBasePath(String path){
        if(OS_NAME.startsWith("win")){
            basePathWin = path;
        }else{
            basePathMac = path;
        }
    }

    public static void needRemoveCache(boolean need){
        needRemoveCache = need;
    }

    public static boolean isNeedRemoveCache(){
        return needRemoveCache;
    }

    public static String getSEP(){
        return SEP;
    }

    public static ArrayList<String> getExtractedResources(){
        return extractingResources;
    }

}
