package me.jissee.pilib;

import com.mojang.logging.LogUtils;
import me.jissee.pilib.event.ClientEventHandler;
import me.jissee.pilib.event.ServerEventHandler;
import me.jissee.pilib.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;


@Mod(PILib.MODID)
public class PILib
{
    public static final String MODID = "pilib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PILib(){
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onCommonSetup);
        me.jissee.pilib.test.Main.setuptest();
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


}
