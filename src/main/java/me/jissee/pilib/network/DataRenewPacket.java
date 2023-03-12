package me.jissee.pilib.network;

import me.jissee.pilib.resource.Texture2D;
import me.jissee.pilib.resource.Texture2DManager;
import me.jissee.pilib.resource.TextureControlCode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import static me.jissee.pilib.PILib.LOGGER;

public class DataRenewPacket implements Iterable<SyncAnimData> {
    public final int length;
    public final ArrayList<SyncAnimData> data = new ArrayList<>();
    public DataRenewPacket(ArrayList<Texture2DManager> managerList){

        length = managerList.size();
        Texture2DManager manager;
        int entityId;
        int ptr;
        Texture2D texture2D;
        TextureControlCode code;
        int progress;
        for(int i = 0; i < length; i++){
            manager = managerList.get(i);
            entityId = manager.getEntityId();
            ptr = manager.getTexturePtr();
            texture2D = manager.getTextureSet();
            code = texture2D.getStatusCode();
            progress = texture2D.getProgress();
            data.add(new SyncAnimData(entityId,ptr,code,progress));
        }
    }
    public DataRenewPacket(FriendlyByteBuf buf){
        length = buf.readInt();
        for(int i = 0; i < length; i++){
            data.add(buf.readOptional(SyncAnimData::readFromBuf).get());
        }
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeInt(length);
        for(int i = 0; i < length; i++){
            buf.writeOptional(Optional.of(data.get(i)),SyncAnimData::writeToBuf);
        }
    }



    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(FMLLoader.getDist().isClient()){
                LOGGER.info("Receiving renew animation data from the server.");
                Texture2DManager.onRenewData(this);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @NotNull
    @Override
    public Iterator<SyncAnimData> iterator() {
        return data.iterator();
    }
}
