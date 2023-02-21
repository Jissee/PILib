package me.jissee.entityrenderlib2d.test;

import me.jissee.entityrenderlib2d.resource.ResourceUtil;
import me.jissee.entityrenderlib2d.test.client.MSoundEvents;
import me.jissee.entityrenderlib2d.test.entity.MEntityTypes;
import me.jissee.entityrenderlib2d.test.entity.TestEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

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
