package me.jissee.pilib.network;

import me.jissee.pilib.resource.TextureControlCode;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;

/**
 * 实体动画同步数据记录，表示对某个实体进行了操作
 */
public final class AnimSyncData {
    private final int entityId;
    private final int ptr;
    private final TextureControlCode code;
    private final int progress;

    public AnimSyncData(int entityId, int ptr, TextureControlCode code, int progress) {
        this.entityId = entityId;
        this.ptr = ptr;
        this.code = code;
        this.progress = progress;
    }

    public static void writeToBuf(FriendlyByteBuf buf, AnimSyncData data) {
        buf.writeInt(data.entityId);
        buf.writeInt(data.ptr);
        buf.writeEnum(data.code);
        buf.writeInt(data.progress);
    }

    public static AnimSyncData readFromBuf(FriendlyByteBuf buf) {
        return new AnimSyncData(
                buf.readInt(),
                buf.readInt(),
                buf.readEnum(TextureControlCode.class),
                buf.readInt()
        );
    }

    public int entityId() {
        return entityId;
    }

    public int ptr() {
        return ptr;
    }

    public TextureControlCode code() {
        return code;
    }

    public int progress() {
        return progress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AnimSyncData) obj;
        return this.entityId == that.entityId &&
                this.ptr == that.ptr &&
                Objects.equals(this.code, that.code) &&
                this.progress == that.progress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, ptr, code, progress);
    }

    @Override
    public String toString() {
        return "SyncAnimData[" +
                "entityId=" + entityId + ", " +
                "ptr=" + ptr + ", " +
                "code=" + code + ", " +
                "progress=" + progress + ']';
    }

}
