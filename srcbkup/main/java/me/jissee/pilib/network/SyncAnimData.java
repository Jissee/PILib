package me.jissee.pilib.network;

import me.jissee.pilib.resource.TextureControlCode;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.UUID;

public record SyncAnimData(int entityId, int ptr, TextureControlCode code, int progress) {

    public static void writeToBuf(FriendlyByteBuf buf, SyncAnimData data) {
        buf.writeInt(data.entityId);
        buf.writeInt(data.ptr);
        buf.writeEnum(data.code);
        buf.writeInt(data.progress);
    }

    public static SyncAnimData readFromBuf(FriendlyByteBuf buf) {
        return new SyncAnimData(
                buf.readInt(),
                buf.readInt(),
                buf.readEnum(TextureControlCode.class),
                buf.readInt()
        );
    }
}
