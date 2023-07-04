package me.jissee.pilib.test.event;

import me.jissee.pilib.event.TextureChangingEvent;
import me.jissee.pilib.event.VideoFinishedPlayingEvent;
import me.jissee.pilib.test.entity.MEntityTypes;
import me.jissee.pilib.test.entity.TestEntity;
import me.jissee.pilib.test.entity.XYY;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class TestServerEventHandler {
    public static void init(IEventBus eventBus){
        eventBus.addListener(TestServerEventHandler::onEntityAttributeCreation);
        eventBus.addListener(TestServerEventHandler::onVideoFinished);
    }

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(MEntityTypes.TEST_ENTITY.get(), TestEntity.prepareAttributes().build());
        event.put(MEntityTypes.XYY.get(), XYY.prepareAttributes().build());
    }

    public static void onVideoFinished(VideoFinishedPlayingEvent event){
        event.getTexture2DManager().change(event.getVideoPtr(), 0);
    }
}
