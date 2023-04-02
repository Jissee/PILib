package me.jissee.pilib.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

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
        INSTANCE.registerMessage(id++, AnimSyncPacket.class, AnimSyncPacket::encode, AnimSyncPacket::new, AnimSyncPacket::handle);
        INSTANCE.registerMessage(id++, T2DMRenewPacket.class, T2DMRenewPacket::encode, T2DMRenewPacket::new, T2DMRenewPacket::handle);
        INSTANCE.registerMessage(id++, T2DMSyncPacket.class, T2DMSyncPacket::encode, T2DMSyncPacket::new, T2DMSyncPacket::handle);
    }
}
