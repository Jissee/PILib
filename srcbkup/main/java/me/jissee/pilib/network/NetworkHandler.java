package me.jissee.pilib.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;

import static me.jissee.pilib.PILib.MODID;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets(){
        INSTANCE.registerMessage(id++, DataSyncPacket.class, DataSyncPacket::encode, DataSyncPacket::new, DataSyncPacket::handle);
        INSTANCE.registerMessage(id++, DataRenewPacket.class, DataRenewPacket::encode, DataRenewPacket::new, DataRenewPacket::handle);
        INSTANCE.registerMessage(id++, SoundStopPacket.class, SoundStopPacket::encode, SoundStopPacket::new, SoundStopPacket::handle);

    }
}
