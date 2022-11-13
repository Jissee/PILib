package me.jissee.entityrenderlib2d;

import com.mojang.logging.LogUtils;
import me.jissee.entityrenderlib2d.resource.ResourceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;


/**
 * Support Windows and Mac only.
 */
@Mod(EntityRenderLib2D.MODID)
public class EntityRenderLib2D
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "entityrenderlib2d";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public EntityRenderLib2D(){
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }



    @SubscribeEvent
    public void Finalize(GameShuttingDownEvent event){
        if(ResourceUtil.isNeedRemoveCache()){
            LOGGER.info("Removing all cached files");
            ResourceUtil.removeAll();
        }
    }


}
