package me.jissee.pilib.test.client;


import me.jissee.pilib.test.client.render.TestEntityRenderer;
import me.jissee.pilib.test.entity.MEntityTypes;
import me.jissee.pilib.test.entity.TestEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.jissee.pilib.PILib.MODID;


@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MClientEventSubscriber {

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MEntityTypes.TEST_ENTITY.get(), TestEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(MEntityTypes.TEST_ENTITY.get(), TestEntity.prepareAttributes().build());
    }


}
