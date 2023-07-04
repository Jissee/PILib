package me.jissee.pilib.network;

import me.jissee.pilib.resource.Texture2DManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import static me.jissee.pilib.PILib.LOGGER;

/**
 * 新玩家登陆时,服务器向其发送所有相关数据
 */
public class T2DMRenewPacket implements Iterable<Texture2DManager.Record> {
    public final int length;
    public final ArrayList<Texture2DManager.Record> data = new ArrayList<>();
    public T2DMRenewPacket(ArrayList<Texture2DManager.Record> managerList){
        length = managerList.size();
        data.addAll(managerList);
    }
    public T2DMRenewPacket(FriendlyByteBuf buf){
        length = buf.readInt();
        for(int i = 0; i < length; i++){
            data.add(buf.readOptional(Texture2DManager.Record::readFromBuf).get());
        }
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeInt(length);
        for(int i = 0; i < length; i++){
            buf.writeOptional(Optional.of(data.get(i)),Texture2DManager.Record::writeToBuf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT){
                LOGGER.debug("Receiving renew animation data from the server.");
                Texture2DManager.onManagerRenew(this);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @NotNull
    @Override
    public Iterator<Texture2DManager.Record> iterator() {
        return data.iterator();
    }
}
