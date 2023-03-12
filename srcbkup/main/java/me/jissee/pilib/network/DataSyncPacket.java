package me.jissee.pilib.network;

import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.Texture2DManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class DataSyncPacket {
    public final UUID senderId;
    public final SyncAnimData data;
    public final boolean keepOld;

    public DataSyncPacket(FriendlyByteBuf buf){
        this.senderId = buf.readUUID();
        this.data = buf.readOptional(SyncAnimData::readFromBuf).get();
        this.keepOld = buf.readBoolean();
    }
    public DataSyncPacket(UUID senderId, SyncAnimData data){
        this.senderId = senderId;
        this.data = data;
        this.keepOld = false;
    }
    public DataSyncPacket(UUID senderId, SyncAnimData data, boolean keepOld){
        this.senderId = senderId;
        this.data = data;
        this.keepOld = keepOld;
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeUUID(senderId);
        buf.writeOptional(Optional.of(data), SyncAnimData::writeToBuf);
        buf.writeBoolean(keepOld);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(FMLLoader.getDist().isClient()){
                Minecraft mc = Minecraft.getInstance();
                UUID uuid = mc.player.getUUID();
                if(!uuid.equals(senderId)){
                    Entity entity = mc.level.getEntity(data.entityId());
                    if(entity instanceof Renderable2D entity2D){
                        entity2D.getTexture2DManager().onSyncData(this.data,keepOld);
                    }
                }
            }else{
                ServerLevel serverLevel = (ServerLevel) ctx.get().getSender().level;
                Entity entity = serverLevel.getEntity(data.entityId());
                if(entity instanceof Renderable2D entity2D){
                    entity2D.getTexture2DManager().onSyncData(this.data,keepOld);
                }
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), this);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
