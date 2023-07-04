package me.jissee.pilib.network;

import me.jissee.pilib.render.Renderable2D;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

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
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
