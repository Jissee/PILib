package me.jissee.entityrenderlib2d.test;

import com.mojang.logging.LogUtils;
import me.jissee.entityrenderlib2d.test.client.MSoundEvents;
import me.jissee.entityrenderlib2d.test.entity.MEntityTypes;
import me.jissee.entityrenderlib2d.test.entity.TestEntity;
import me.jissee.entityrenderlib2d.resource.ResourceUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import static me.jissee.entityrenderlib2d.EntityRenderLib2D.MODID;


/**
 * Support Windows and Mac only.
 */

public class Main
{
    public static void setuptest(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MEntityTypes.ENTITY_TYPES.register(modEventBus);
        MSoundEvents.SOUNDEVENTS.register(modEventBus);

        if(FMLLoader.getDist().isDedicatedServer()){
            modEventBus.addListener(
                    (EntityAttributeCreationEvent e) -> e.put(MEntityTypes.TEST_ENTITY.get(), TestEntity.prepareAttributes().build()));
        }
        ResourceUtil.addExclusion(MODID, "ffmpeg.exe");
        ResourceUtil.register(new ResourceLocation(MODID,"video/qmdyj-full.mp4"));

    }


}
