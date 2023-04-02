package me.jissee.pilib.network;

import me.jissee.pilib.render.Renderable2D;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Supplier;

public class AnimSyncPacket {
    public final AnimSyncData data;

    public AnimSyncPacket(FriendlyByteBuf buf){
        this.data = buf.readOptional(AnimSyncData::readFromBuf).get();
    }
    public AnimSyncPacket(AnimSyncData data){
        this.data = data;
    }
    public void encode(FriendlyByteBuf buf){
        buf.writeOptional(Optional.of(data), AnimSyncData::writeToBuf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT){
                Minecraft mc = Minecraft.getInstance();
                Entity entity = mc.level.getEntity(data.entityId());
                if(entity instanceof Renderable2D entity2D){
                    entity2D.getTexture2DManager().onAnimSync(this.data);
                }
            }else{
                ServerLevel serverLevel = (ServerLevel) ctx.get().getSender().level;
                Entity entity = serverLevel.getEntity(data.entityId());
                if(entity instanceof Renderable2D entity2D){
                    entity2D.getTexture2DManager().onAnimSync(this.data);
                }
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), this);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
