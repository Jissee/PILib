package me.jissee.pilib.event;

import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.network.T2DMRenewPacket;
import me.jissee.pilib.network.T2DMSyncPacket;
import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.LocalResourceUtil;
import me.jissee.pilib.resource.MSoundEngine;
import me.jissee.pilib.resource.Texture2DManager;
import me.jissee.pilib.resource.VideoResource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
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
            T2DMRenewPacket renewPacket = Texture2DManager.getRenewData(event.getEntity().getServer());
            LOGGER.debug("Sending SyncAnimData to clients");
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->serverPlayer), renewPacket);
        }
    }

    @SubscribeEvent
    public static void onEntityChangeDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Renderable2D entity2d){
            if(entity.level.isClientSide){
                if(entity2d.getTexture2DManager().getTextureSet() instanceof VideoResource videoResource){
                    MSoundEngine.stopVideoSound(videoResource);
                }
            }else{
                entity2d.saveData();
            }
        }
    }

    @SubscribeEvent
    public static void loadDataOnEntityJoin(EntityJoinLevelEvent event){
        Level lvl = event.getEntity().getLevel();
        Entity entity = event.getEntity();
        if(entity instanceof Renderable2D entity2d){
            if(lvl instanceof ServerLevel){
                entity2d.loadData();
            }else if(lvl instanceof ClientLevel){
                T2DMSyncPacket packet = new T2DMSyncPacket(entity.getId());
                NetworkHandler.INSTANCE.sendToServer(packet);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event){
        Level lvl = event.getLevel();
        Entity entity = event.getEntity();
        if(entity instanceof Renderable2D entity2d){
            if(lvl instanceof ServerLevel) {
                entity2d.saveData();
            }else if(lvl instanceof ClientLevel){
                if(entity2d.getTexture2DManager().getTextureSet() instanceof VideoResource videoResource){
                    MSoundEngine.stopVideoSound(videoResource);
                }
            }
        }

    }

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
    }

    @SubscribeEvent
    public static void finalizeOnShuttingDown(GameShuttingDownEvent event){
        LocalResourceUtil.FinalizeAll();
    }


}
