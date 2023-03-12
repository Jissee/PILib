package me.jissee.pilib.event;

import me.jissee.pilib.network.DataRenewPacket;
import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.LocalResourceUtil;
import me.jissee.pilib.resource.Texture2DManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import static me.jissee.pilib.PILib.LOGGER;

public final class ServerEventHandler {

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
    }/*
    @SubscribeEvent
    public static void saveDataOnLevelSave(LevelEvent.Save event){
        if(event.getLevel() instanceof ServerLevel slvl){
            for(Entity entity : slvl.getAllEntities()){
                if(entity instanceof Renderable2D entity2d){
                    entity2d.saveData();
                }
            }
        }
    }*/

    @SubscribeEvent
    public static void saveDataOnServerSave(ServerStoppingEvent event){
        for(Level lvl : event.getServer().getAllLevels()){
            if(lvl instanceof ServerLevel slvl){
                for(Entity entity : slvl.getAllEntities()){
                    if(entity instanceof Renderable2D entity2d){
                        entity2d.saveData();
                    }
                }
            }
        }
        LOGGER.info("Removing all Texture2DManagers.");
        Texture2DManager.clearTextureManagers();
    }

    @SubscribeEvent
    public static void finalizeOnShuttingDown(GameShuttingDownEvent event){
        LocalResourceUtil.FinalizeAll();
    }


}
