package me.jissee.pilib;

import com.mojang.logging.LogUtils;
import me.jissee.pilib.resource.ResourceUtil;
import me.jissee.pilib.test.Main;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;


/**
 * Support Windows and Mac only.
 */
@Mod(PILib.MODID)
public class PILib
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "pilib";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public PILib(){
        Main.setuptest();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void Finalize(GameShuttingDownEvent event){
        ResourceUtil.Finalize();
    }


}
