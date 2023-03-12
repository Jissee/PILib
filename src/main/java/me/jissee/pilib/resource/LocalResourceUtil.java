package me.jissee.pilib.resource;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;

/**
 * 本地资源管理器<br/>
 * 管理视频等需要解压到本地到文件<br/>
 * 本地缓存位置：<br/>
 * Windows: D:\MCCache<br/>
 * Mac/Linux: /$HOME/MCCache<br/><br/>
 * Used to manage resources like videos that will be extracted to local cache file<br/>
 * Cache folders:<br/>
 * Windows: D:\MCCache<br/>
 * Mac/Linux: /$HOME/MCCache<br/>
 *
 */
public class LocalResourceUtil {
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String SEP = File.separator;
    private static final String basePathWin = "D:" + SEP + "MCCache" + SEP;
    private static final String basePathMacLin = System.getenv("HOME") + SEP + "MCCache" + SEP;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ArrayList<LocalResourceUtil> allUtils = new ArrayList<>();
    private final String MOD_ID;
    private final ArrayList<ResourceLocation> localResources = new ArrayList<>();
    private final boolean removeCache;
    private final boolean removeDecoded;

    /**
     *
     * @param removeCache    是否需要在退出游戏时删除所有缓存文件 Whether the cached file need to be removed when quitting the game.<br/>
     * @param removeDecoded  是否需要在退出游戏时删除所有解码文件 Whether the decoded file need to be removed when quitting the game.<br/><br/>
     *                        注意：游戏如果崩溃或者非正常退出，缓存文件不会被删除<br/>
     *
     *                        Note that if the game is crashed or exit unexpectedly, cached file will not be removed
     */
    public LocalResourceUtil(String MOD_ID, boolean removeCache, boolean removeDecoded){
        this.MOD_ID = MOD_ID;
        this.removeCache = removeCache;
        this.removeDecoded = removeDecoded;
        allUtils.add(this);
    }

    /**
     * 注册需要被解压到本地缓存文件夹的资源<br/>
     * Register resources that needs to be extracted to local cache folder.
     */
    public void register(ResourceLocation location){
        localResources.add(location);
    }

    /**
     * 获得在jar包内部的路径
     * Get the path in jar file.
     */
    public String getInJarPath(ResourceLocation location){
        return "assets/" + location.getNamespace() + "/" + location.getPath();
    }

    /**
     * 获取缓存文件夹基址<br/>
     * Get the base path of cached file folder.
     */
    private static String getBasePath(){
        String path;
        if(OS_NAME.startsWith("win")){
            path = basePathWin;
        }else{
            path = basePathMacLin;
        }
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return path;
    }
    /**
     * 获取对应运行环境的缓存文件夹基址<br/>
     * Get the base path of cached file folder for corresponding side environment.
     */
    private static String getSideBasePath(){
        String path;
        if(FMLLoader.getDist() == null || FMLLoader.getDist().isClient()){
            path = getBasePath() + "client" + SEP;
        }else{
            path = getBasePath() + "server" + SEP;
        }
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        return path;
    }


    /**
     * 获取解码后文件的路径<br/>
     * Get the decoded file path.
     */
    public String getModDecodeBasePath(){
        String path = getSideBasePath() + "decode" + SEP + MOD_ID + SEP;
        File decPath = new File(path);
        if(!decPath.exists()){
            decPath.mkdirs();
        }
        return path;
    }


    /**
     * 解压指定{@link ResourceLocation}的资源<br/>
     * Extract a resource by its {@link ResourceLocation}.
     */
    public void extract(ResourceLocation location){

        String injarPath = getInJarPath(location);
        InputStream ins = LocalResourceUtil.class.getClassLoader().getResourceAsStream(injarPath);
        String fPath = getLocalFileName(location);

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
            LOGGER.info("Extracted file: " + fPath);
            ins.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 按下标解压材质文件<br/>
     * Extract a specific registered texture from jar to local cache folder by index.
     */
    public void extract(int index){
        extract(localResources.get(index));
    }

    /**
     * 解压所有注册的文件<br/>
     * Extract all registered textures from jar to local cache folder.
     */
    public void extractAll()  {
        for(ResourceLocation location : localResources){
            extract(location);
        }
    }

    /**
     * 递归删除文件，不应该被其他类直接使用<br/>
     * Remove the files recursively, which should not be directly used by other classes.
     */
    private static void removeRecursively(String path){
        File fl = new File(path);
        File[] files = fl.listFiles();

        for(int i = 0; i < files.length; i++){
            if (files[i].isDirectory()){
                removeRecursively(files[i].getAbsolutePath());
            }
            files[i].delete();

        }
    }

    /**
     * 通过{@link ResourceLocation}获取对应解压到本地后的文件名<br/>
     * Get the extracted local file name corresponding to {@link ResourceLocation}。
     */
    public String getLocalFileName(ResourceLocation location){
        return getSideBasePath() + getInJarPath(location).replace('/', SEP.charAt(0)).replace('\\', SEP.charAt(0));
    }

    /**
     * 通过下标获取对应解压到本地后的文件名<br/>
     * Get the extracted local file name by index.
     */
    public String getLocalFileName(int index){
        return getLocalFileName(localResources.get(index));
    }

    /**
     * 删除此mod注册的所有缓存文件<br/>
     * Remove all cached files registered by this mod.
     *
     */
    public void removeExtracted(){
        removeRecursively(getSideBasePath() + "assets/" + MOD_ID);
        LOGGER.info("Cached file removed!");
    }
    /**
     * 删除此mod产生的所有解码后的文件<br/>
     * Remove all decoded files from this mod.
     *
     */
    public void removeDecoded(){
        removeRecursively(getSideBasePath() + "decode/" + MOD_ID);
        LOGGER.info("Decoded file removed!");
    }

    /**
     * 获取ffmpeg地址<br/>
     * Get the path of ffmpeg path.
     * @throws IOException 若ffmpeg不存在 If ffmpeg does not exist.
     */
    public static String getFfmpegPath() throws IOException {
        StringBuilder path = new StringBuilder();
        path.append(getBasePath());
        if(OS_NAME.startsWith("win")){
            path.append("ffmpeg.exe");
        }else{
            path.append("ffmpeg");
        }
        if(!new File(path.toString()).exists()){
            throw new FileNotFoundException("Ffmpeg does not exist. Expected in " + path);
        }
        return path.toString();
    }

    /**
     * 文件目录分割符
     * The separator of the directory
     */
    public static String getSEP(){
        return SEP;
    }

    public void Finalize(){
        LOGGER.info("Trying to remove all cached files for " + MOD_ID);
        if(removeCache){
            removeExtracted();
        }
        LOGGER.info("Trying to remove all decoded files for " + MOD_ID);
        if(removeDecoded){
            removeDecoded();
        }
    }

    public static void FinalizeAll(){
        for(LocalResourceUtil util : allUtils){
            util.Finalize();
        }
        allUtils.clear();
    }

    /**
     * 把帧率转换为纳秒时间间隔
     * fps to nanoInterval
     */
    public static long toNanoInterval(double fps){
        return (long)((1.0 / fps) * 1e9);
    }
    /**
     * 把纳秒时间间隔转换为帧率
     * nanoInterval to fps
     */
    public static double toFPS(long nanoInterval){
        return (1.0 / (nanoInterval / 1e9));
    }




}
