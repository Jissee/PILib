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
import java.util.HashMap;
import java.util.Map;

public class ResourceUtil {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<String, ArrayList<String>> resourceMapIncl = new HashMap<>();
    private static final HashMap<String, ArrayList<String>> resourceMapExcl = new HashMap<>();
    private static final HashMap<String, Boolean> needRemove = new HashMap<>();
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String SEP = File.separator;
    private static String basePathWin = "D:" + SEP + "MCCache" + SEP;
    private static String basePathMac = SEP + "Users" + SEP + "MCCache" + SEP;

    static{
        File path = new File(getBasePath());
        if(!path.exists()){
            path.mkdir();
        }
    }

    /**
     * 注册需要被解压到本地缓存文件夹的文件<br/>
     * Register textures that needs to be extracted to local cache folder.
     */
    public static void register(ResourceLocation location){
        ArrayList<String> list;
        String modId = location.getNamespace();
        if((list = resourceMapIncl.get(modId)) == null){
            list = new ArrayList<>();
            list.add("assets/" + location.getNamespace() + "/" + location.getPath());
            resourceMapIncl.put(modId,list);
        }else{
            list.add("assets/" + location.getNamespace() + "/" + location.getPath());
        }
    }

    /**
     * 按下表解压材质文件<br/>
     * Extract a specific registered texture from jar to local cache folder.
     */
    public static void extract(String modId, int index){
        String injarPath = resourceMapIncl.get(modId).get(index);
        InputStream ins = ResourceUtil.class.getClassLoader().getResourceAsStream(injarPath);
        String fPath = (getBasePath() + injarPath).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));

        String dPath = "";
        for(int i = fPath.length() - 1; i >= 0; i--){
            if(fPath.charAt(i) == SEP.charAt(0)){
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
     * 解压所有注册的文件<br/>
     * Extract all registered textures from jar to local cache folder.
     */
    public static void extractAll(String modId)  {
        int size = resourceMapIncl.get(modId).size();
        for(int i = 0; i < size; i++){
            extract(modId,i);
        }
    }

    private static void removeRecursively(String path, ArrayList<String> listExcl){
        File fl = new File(path);
        File[] files = fl.listFiles();
        for(int i = 0; i < files.length; i++){
            if (files[i].isDirectory()){
                removeRecursively(files[i].getAbsolutePath(),listExcl);
            }
            for(String exclusion : listExcl){
                if(files[i].getName().endsWith(exclusion)){
                    break;
                }
                files[i].delete();
            }

        }
    }

    /**
     * 添加删除例外，即不需要被删除的缓存文件（无需路径）<br/>
     * Add file name (without path), which will not be removed by removeAll
     */
    public static void addExclusion(String modId, String excl) {
        ArrayList<String> listExcl;
        if((listExcl = resourceMapExcl.get(modId)) == null){
            listExcl = new ArrayList<>();
            listExcl.add(excl);
            resourceMapExcl.put(modId, listExcl);
        }else{
            listExcl.add(excl);
        }
    }

    /**
     * 【危险操作】删除此mod注册的所有缓存文件，除了例外文件<br/>
     * [DANGER] Remove all cached files registered by this mod. If a file is added to the exclusion, it will not be removed.
     *
     */
    public static void removeAll(String modid){
        if(modid.equals("entityrenderlib2d")){
            LOGGER.warn("DO NOT remove Library's files");
        }else{
            removeRecursively(getBasePath() + "assets/" + modid, resourceMapExcl.get(modid));
            LOGGER.info("Old file removed!");
        }

    }

    /**
     * 通过下标获取文件名<br/>
     * Get the registered file name by index.
     */
    public static String getFileName(String modId, int index){
        String fPath;
        fPath = (getBasePath() + resourceMapIncl.get(modId).get(index)).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
        return fPath;
    }

    /**
     * 获取缓存文件夹基址<br/>
     * Get the base path of cached file folder.
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
     * 获取ffmpeg地址<br/>
     * Get the path of ffmpeg path.
     * @throws IOException 若ffmpeg不存在 If ffmpeg does not exist.
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
     * 为Windows设置缓存文件夹基址<br/>
     * Set the cache file folder path for Windows.
     */
    public static void setBasePathWin(String path){
        basePathWin = path;
    }
    /**
     * 为Mac设置缓存文件夹基址<br/>
     * Set the cache file folder path for Mac.
     */
    public static void setBasePathMac(String path){
        basePathMac = path;
    }

    /**
     * 是否需要在退出游戏时删除所有缓存文件<br/>
     * 注意：游戏如果崩溃或者非正常退出，缓存文件不会被删除<br/><br/>
     * Whether the cached file need to be removed when quitting the game.<br/>
     * Note that if the game is crashed or exit unexpectedly, cached file will not be removed
     *
     */
    public static void needRemoveCache(String modId, boolean need){
        needRemove.put(modId,need);
    }


    public static boolean isNeedRemoveCache(String modId){
        return needRemove.get(modId);
    }


    public static String getSEP(){
        return SEP;
    }

    public static void Finalize(){
        LOGGER.info("Trying to remove all cached files");
        String modId;
        boolean need;
        for(Map.Entry<String, Boolean> entry : needRemove.entrySet()){
            need = entry.getValue();
            if(need){
                modId = entry.getKey();
                removeAll(modId);
            }
        }
    }

}
