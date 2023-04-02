package me.jissee.pilib.network;

import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.Texture2DManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Supplier;

public class T2DMSyncPacket {
    public final int entityId;
    private final Texture2DManager.Record record;

    public T2DMSyncPacket(Texture2DManager.Record managerRecord){
        this.entityId = -1;
        this.record = managerRecord;
    }
    public T2DMSyncPacket(int entityId){
        this.entityId = entityId;
        this.record = Texture2DManager.Record.EMPTY;
    }
    public T2DMSyncPacket(FriendlyByteBuf buf){
        entityId = buf.readInt();
        record = buf.readOptional(Texture2DManager.Record::readFromBuf).get();
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeInt(entityId);
        buf.writeOptional(Optional.of(record),Texture2DManager.Record::writeToBuf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT){
                Minecraft mc = Minecraft.getInstance();
                Entity entity = mc.level.getEntity(record.entityId);
                if(entity instanceof Renderable2D entity2d){
                    entity2d.getTexture2DManager().updateBy(record);
                }
            }else{
                MinecraftServer svr = ctx.get().getSender().getServer();
                for(ServerLevel slvl : svr.getAllLevels()){
                    Entity entity = slvl.getEntity(entityId);
                    if(entity instanceof  Renderable2D entity2d){
                        Texture2DManager.Record record = entity2d.getTexture2DManager().toRecord();
                        T2DMSyncPacket packet = new T2DMSyncPacket(record);
                        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(()->ctx.get().getSender()), packet);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public Texture2DManager.Record getRecordData(){
        return record;
    }

}
