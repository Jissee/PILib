package me.jissee.pilib.resource;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalResourceUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String MoDiD;
    private final ArrayList<String> resourceIncl = new ArrayList<>();
    private final ArrayList<String> resourceExcl = new ArrayList<>();
    private boolean needRemove;
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final String SEP = File.separator;
    private static String basePathWin = "D:" + SEP + "MCCache" + SEP;
    private static String basePathMac = SEP + "Users" + SEP + "MCCache" + SEP;

    public LocalResourceUtil(String modId){
        this.MoDiD = modId;
    }

    /**
     * 注册需要被解压到本地缓存文件夹的文件<br/>
     * Register textures that needs to be extracted to local cache folder.
     */
    public void register(ResourceLocation location){
        resourceIncl.add("assets/" + location.getNamespace() + "/" + location.getPath());
    }

    /**
     * 按下标解压材质文件<br/>
     * Extract a specific registered texture from jar to local cache folder.
     */
    public void extract(String modId, int index){
        String injarPath = resourceIncl.get(modId).get(index);
        ResourceProvider prov;
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
            LOGGER.info("Extracted file: " + fPath);
            ins.close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
