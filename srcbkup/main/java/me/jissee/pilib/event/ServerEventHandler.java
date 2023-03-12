package me.jissee.pilib.event;

import me.jissee.pilib.network.DataRenewPacket;
import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.Texture2D;
import me.jissee.pilib.resource.Texture2DManager;
import me.jissee.pilib.resource.TextureSetting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.core.jmx.Server;

import static me.jissee.pilib.PILib.LOGGER;

public final class ServerEventHandler {
    public static void registerServerEvents(IEventBus eventBus){
        eventBus.addListener(ServerEventHandler::syncOnPlayerJoin);
        eventBus.addListener(ServerEventHandler::onServerTick);
        eventBus.addListener(ServerEventHandler::loadDataOnEntityJoin);
        eventBus.addListener(ServerEventHandler::saveDataOnLevelSave);
    }

    @SubscribeEvent
    public static void syncOnPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        if(player instanceof ServerPlayer serverPlayer){
            DataRenewPacket renewPacket = Texture2DManager.getRenewData();
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->serverPlayer), renewPacket);
        }
    }
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        Texture2DManager.tickAll();
    }
    @SubscribeEvent
    public static void loadDataOnEntityJoin(EntityJoinLevelEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Renderable2D entity2d){
            entity2d.loadData();
        }
    }
    @SubscribeEvent
    public static void saveDataOnLevelSave(ServerStoppingEvent event){
        for(Level lvl : event.getServer().getAllLevels()){
            if(lvl instanceof ServerLevel slvl){
                for(Entity entity : slvl.getAllEntities()){
                    if(entity instanceof Renderable2D entity2d){
                        entity2d.saveData();
                    }
                }
            }
        }
    }


}
