package me.jissee.pilib;

import com.mojang.logging.LogUtils;
import me.jissee.pilib.event.ClientEventHandler;
import me.jissee.pilib.event.ServerEventHandler;
import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.resource.RemapUtil;
import me.jissee.pilib.resource.ResourceUtil;
import me.jissee.pilib.test.Main;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
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
    public static final Logger LOGGER = LogUtils.getLogger();

    public PILib(){
        RemapUtil.init();
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onCommonSetup);


        Main.setuptest();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ServerEventHandler.class);

        if(FMLLoader.getDist().isClient()){
            MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        }

    }
    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event){
        NetworkHandler.registerPackets();
    }


    @SubscribeEvent
    public void Finalize(GameShuttingDownEvent event){
        ResourceUtil.Finalize();
    }


}
