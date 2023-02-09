package me.jissee.entityrenderlib2d.resource;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
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

    static{
        File path = new File(getBasePath());
        if(!path.exists()){
            path.mkdir();
        }
    }

    /**
     * Register textures that needs to be extracted to local cache folder.
     * @param location Texture ResourceLocation.
     */
    public static void register(ResourceLocation location){
        extractingResources.add("assets/" + location.getNamespace() + "/" + location.getPath());
    }

    /**
     * Extract a specific registered texture from jar to local cache folder.
     *
     * @param index The index of the texture.
     */
    public static void extract(int index){
        InputStream ins = ResourceUtil.class.getClassLoader().getResourceAsStream(extractingResources.get(index));
        String fPath;

        fPath = (getBasePath() + extractingResources.get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));

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

    /**
     * Extract all registered textures from jar to local cache folder.
     *
     *
     */
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

    /**
     * Add file name (without path), which will not be removed by removeAll
     * @param exc
     */
    public static void addExclusion(String exc) {
        removeExclusion.add(exc);
    }

    /**
     * Remove all cached file.<bt/>
     * If a file is added to the exclusion, it will not be removed.
     *
     */
    public static void removeAll(){
        removeRecursively(getBasePath());
        LOGGER.info("Old file removed!");
    }

    /**
     * Get the registered file name by index.
     * @return Full path of file at index.
     */
    public static String getFileName(int index){
        String fPath;
        fPath = (getBasePath() + extractingResources.get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
        return fPath;
    }

    /**
     * Get the base path of cached file folder.
     * @return
     */
    public static String getBasePath(){
        if(OS_NAME.startsWith("win")){
            if(FMLLoader.getDist().isDedicatedServer()){
                return basePathWin + "server" + SEP;
            }else{
                return basePathWin + "client" + SEP;
            }
        }else{
            if(FMLLoader.getDist().isDedicatedServer()){
                return basePathMac + "server" + SEP;
            }else{
                return basePathMac + "client" + SEP;
            }
        }
    }

    /**
     *  Get the path of ffmpeg path.
     * @throws IOException If ffmpeg does not exist.
     */
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

    /**
     * Set the cache file folder path for Windows.
     */
    public static void setBasePathWin(String path){
            basePathWin = path;
    }
    /**
     * Set the cache file folder path for Mac.
     */
    public static void setBasePathMac(String path){
            basePathMac = path;
    }

    /**
     * Whether the cached file need to be removed when quitting the game.<br/>
     * Note that if the game is crashed or exit unexpectedly, cached file will not be removed
     *
     */
    public static void needRemoveCache(boolean need){
        needRemoveCache = need;
    }


    public static boolean isNeedRemoveCache(){
        return needRemoveCache;
    }

    /**
     *
     * @return separating symbol of the system
     */
    public static String getSEP(){
        return SEP;
    }

}
