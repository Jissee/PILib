package me.jissee.pilib.network;

import me.jissee.pilib.resource.TextureControlCode;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 实体动画同步数据记录，表示对某个实体进行了操作
 * @param entityId
 * @param ptr
 * @param code
 * @param progress
 */
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
