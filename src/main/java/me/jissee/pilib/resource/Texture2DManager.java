package me.jissee.pilib.resource;

import me.jissee.pilib.event.*;
import me.jissee.pilib.network.AnimSyncData;
import me.jissee.pilib.network.AnimSyncPacket;
import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.network.T2DMRenewPacket;
import me.jissee.pilib.render.Renderable2D;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 在二维实体内使用，被渲染器调用来管理材质<br/><br/>
 * Used in 2D entities to manage texture sets and called by renderer.
 */
public class Texture2DManager implements Iterable<Texture2D> {
    private final ArrayList<Texture2D> textureSets = new ArrayList<>();
    private int texturePtr;
    private final Entity target;
    private boolean autoPause;

    /**
     * 用于客户端接收新数据
     * Client accept new data from server
     */
    public static void onManagerRenew(T2DMRenewPacket packet){
        HashMap<Integer, Record> map = new HashMap<>();
        for(Record managerRecord : packet){
            map.put(managerRecord.entityId, managerRecord);
        }
        int eid;
        Record updatingSource;

        for(Map.Entry<Integer, Record> entry : map.entrySet()){
            eid = entry.getKey();
            updatingSource = entry.getValue();
            Entity entity = Minecraft.getInstance().level.getEntity(eid);
            if(entity instanceof Renderable2D entity2d){
                entity2d.getTexture2DManager().updateBy(updatingSource);
            }
        }
    }

    /**
     * 用于服务端同步数据
     * Used to synchronize data from the server side
     */
    public synchronized void onAnimSync(AnimSyncData data){
        assert target.getId() == data.entityId();
        int ptr = data.ptr();
        TextureControlCode code = data.code();
        int progress = data.progress();

        switch (code){
            case PAUSE -> pauseInternal(ptr, progress);
            case RESUME -> resumeInternal(ptr, progress);
            case CHANGE -> changeInternal(ptr, progress);
        }
    }

    public static T2DMRenewPacket getRenewData(MinecraftServer server){
        Iterable<ServerLevel> slvls = server.getAllLevels();
        ArrayList<Record> managers = new ArrayList<>();
        for(ServerLevel slvl : slvls){
            for(Entity e : slvl.getAllEntities()){
                if(e instanceof Renderable2D e2D){
                    managers.add(e2D.getTexture2DManager().toRecord());
                }
            }
        }
        return new T2DMRenewPacket(managers);
    }
    public synchronized CompoundTag getSaveData(){
        int count = textureSets.size();
        CompoundTag tag = new CompoundTag();
        tag.putInt("count", count);
        tag.putInt("ptr", texturePtr);
        int[] status = new int[count];
        int[] progress = new int[count];
        for(int i = 0; i < count; i++){
            status[i] = textureSets.get(i).getStatusCode().getIndex();
            progress[i] = textureSets.get(i).getProgress();
            if(i == texturePtr && isAutoPause()) status[i] = TextureControlCode.PLAYING.getIndex();
        }
        tag.putIntArray("status", status);
        tag.putIntArray("progress", progress);
        return tag;
    }
    public synchronized void setSaveData(CompoundTag tag){
        int count = tag.getInt("count");
        int ptr = tag.getInt("ptr");
        int[] status = tag.getIntArray("status");
        int[] progress = tag.getIntArray("progress");
        if(count == this.textureSets.size()){
            this.texturePtr = ptr;
            TextureControlCode code;
            for(int i = 0; i < count; i++){
                code = TextureControlCode.values()[status[i]];
                textureSets.get(i).setStatusCode(code);
                textureSets.get(i).setProgress(progress[i]);
            }
        }
    }

    public synchronized void updateBy(Record record){
        if(this.getEntityId() != record.entityId){
            return;
        }
        int count = this.getTextureCount();
        for(int i = 0; i < count; i++){
            this.textureSets.get(i).setStatusCode(record.getStatus(i));
            this.textureSets.get(i).setProgress(record.getProgress(i));
        }
        this.texturePtr = record.texturePtr;
        Texture2D t = this.getTextureSet();
        if(t instanceof VideoResource videoResource){
            TextureControlCode code = this.getTextureSet().getStatusCode();
            int progress = this.getTextureSet().getProgress();
            if(this.target.level().isClientSide){
                if(code == TextureControlCode.PLAYING){
                    Vec3 pos = new Vec3(this.target.getX(), this.target.getY(), this.target.getZ());
                    MSoundEngine.playWithProgressOffset(videoResource, pos, progress);
                }
            }
        }
    }

    /**
     * 创建二维材质管理器
     *
     */
    public Texture2DManager(Entity target){
        this(target, 0);
    }

    /**
     * 创建二维材质管理器
     *
     * @param defaultPtr 默认材质下标指针，空则为0；
     */
    public Texture2DManager(Entity target, int defaultPtr){
        if(!(target instanceof Renderable2D)) throw new IllegalStateException("Entity " + target + " is not 2D.");
        this.target = target;
        this.texturePtr = defaultPtr;

    }

    /**
     * 按顺序添加二维材质，请注意客户端和服务端的同步
     * Add texture by sequence. Please keep data synchronized between clients and servers.
     */
    public Texture2DManager addTextureSet(Texture2D textureSet) {
        this.textureSets.add(textureSet);
        return this;
    }

    /**
     * 获取当前的二维材质
     * Get current texture
     */
    public Texture2D getTextureSet(){
        return this.textureSets.get(texturePtr);
    }


    /**
     * 切换二维材质
     * Change texture
     * @param newPtr      新的下标指针 The new index of the textures.
     * @param progress    新材质的播放进度 The playing time of the new texture.
     */
    public void change(int newPtr, int progress){
        if(MinecraftForge.EVENT_BUS.post(new TextureChangingEvent(this, newPtr, progress))) return;
        changeInternal(newPtr, progress);
        if(!target.level().isClientSide){
            AnimSyncData data = new AnimSyncData(target.getId(),newPtr,TextureControlCode.CHANGE, progress);
            AnimSyncPacket packet = new AnimSyncPacket(data);
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        }

    }
    private synchronized void changeInternal(int newPtr, int progress){
        Texture2D oldt = textureSets.get(texturePtr);
        if(oldt instanceof VideoResource videoResource){
            if(videoResource.getStatusCode() == TextureControlCode.PLAYING){
                if(target.level().isClientSide){
                    MSoundEngine.stopVideoSound(videoResource);
                }
            }
        }
        texturePtr = Math.min(newPtr, textureSets.size() - 1);
        Texture2D t = textureSets.get(texturePtr);

        if(t instanceof VideoResource videoResource){
            if(videoResource.getStatusCode() == TextureControlCode.PLAYING){
                if(target.level().isClientSide){
                    Vec3 pos = new Vec3(target.getX(), target.getY(), target.getZ());
                    MSoundEngine.playWithProgressOffset(videoResource, pos, progress);
                }
            }
        }
        t.setProgress(progress);
    }

    /**
     * （仅单人模式）当游戏暂停时，渲染器会自动暂停当前资源的播放
     * (Single-player only) When the game is paused, the renderer will auto pause the resource.
     */
    public void autoPause(){
        if(!isPaused()){
            pause();
            autoPause = true;
        }
    }

    /**
     * （仅单人模式）当游戏从暂停恢复时，渲染器会自动恢复当前资源的播放
     * (Single-player only) When the game is resumed, the renderer will auto resume the resource.
     */
    public void autoResume(){
        if(autoPause && isPaused()){
            resume();
            autoPause = false;
        }
    }

    /**
     * （仅单人模式）检查当前资源是否由于游戏暂停而自动暂停播放
     * (Single-player only) Check whether the resource is paused due to game pause.
     *
     */
    public boolean isAutoPause(){
        return autoPause;
    }

    /**
     * 暂停当前正在播放的材质
     * Pause the current texture
     */
    public void pause(){
        pause(texturePtr);
    }
    /**
     * 暂停指定下标的材质
     * Pause the texture with index
     */
    public void pause(int ptr){
        if(textureSets.get(ptr).getStatusCode() == TextureControlCode.PAUSE){
            return;
        }
        if(MinecraftForge.EVENT_BUS.post(new TexturePausingEvent(this, ptr, textureSets.get(ptr).getProgress()))) return;
        pauseInternal(ptr, textureSets.get(ptr).getProgress());
        if(!target.level().isClientSide){
            AnimSyncData data = new AnimSyncData(target.getId(),ptr,TextureControlCode.PAUSE, textureSets.get(ptr).getProgress());
            AnimSyncPacket packet = new AnimSyncPacket(data);
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        }
        MinecraftForge.EVENT_BUS.post(new TexturePausedEvent(this, ptr, textureSets.get(ptr).getProgress()));
    }
    private synchronized void pauseInternal(int ptr,int progress){
        texturePtr = ptr;
        Texture2D t = textureSets.get(ptr);
        if(t instanceof VideoResource videoResource){
            if(target.level().isClientSide){
                MSoundEngine.stopVideoSound(videoResource);
            }
        }

        t.pause();
        t.setProgress(progress);
    }
    /**
     * 恢复当前正在播放的材质
     * Resume the current texture
     */
    public void resume(){
        resume(texturePtr);
    }
    /**
     * 恢复指定下标的材质
     * Resume the texture with index
     */
    public void resume(int ptr){
        if(textureSets.get(ptr).getStatusCode() == TextureControlCode.PLAYING){
            return;
        }
        if(MinecraftForge.EVENT_BUS.post(new TextureResumingEvent(this, ptr, textureSets.get(ptr).getProgress()))) return;
        resumeInternal(ptr, textureSets.get(ptr).getProgress());
        if(!target.level().isClientSide){
            AnimSyncData data = new AnimSyncData(target.getId(),ptr,TextureControlCode.RESUME, textureSets.get(ptr).getProgress());
            AnimSyncPacket packet = new AnimSyncPacket(data);
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        }
        MinecraftForge.EVENT_BUS.post(new TextureResumedEvent(this, ptr, textureSets.get(ptr).getProgress()));
    }
    private synchronized void resumeInternal(int ptr, int progress){
        texturePtr = ptr;
        Texture2D t = textureSets.get(ptr);
        if(t instanceof VideoResource videoResource){
            if(target.level().isClientSide){
                Vec3 pos = new Vec3(target.getX(), target.getY(), target.getZ());
                MSoundEngine.playWithProgressOffset(videoResource, pos, progress);
            }
        }
        t.resume();
        t.setProgress(progress);
    }



    public boolean isPaused(){
        return this.textureSets.get(texturePtr).getStatusCode() == TextureControlCode.PAUSE;
    }

    public int getEntityId(){
        return target.getId();
    }
    public int getTexturePtr() {
        return texturePtr;
    }

    /**
     * 获取当前材质的数量
     */
    public int getTextureCount(){
        return textureSets.size();
    }

    @NotNull
    @Override
    public Iterator<Texture2D> iterator() {
        return textureSets.iterator();
    }

    public Record toRecord(){
        return new Record(this);
    }

    public static class Record{
        public static final Record EMPTY = new Record(-1,0,0);
        public final int entityId;
        public final int texturePtr;
        public final int textureCount;
        private final ArrayList<TextureControlCode> status = new ArrayList<>();
        private final ArrayList<Integer> progresses = new ArrayList<>();

        public Record(Texture2DManager manager){
            entityId = manager.getEntityId();
            texturePtr = manager.getTexturePtr();
            textureCount = manager.getTextureCount();
            for(Texture2D t2d : manager){
                status.add(t2d.getStatusCode());
                progresses.add(t2d.getProgress());
            }
        }
        private Record(int entityId, int texturePtr, int textureCount){
            this.entityId = entityId;
            this.texturePtr = texturePtr;
            this.textureCount = textureCount;
        }
        public static void writeToBuf(FriendlyByteBuf buf, Record record){
            buf.writeInt(record.entityId);
            buf.writeInt(record.texturePtr);
            buf.writeInt(record.textureCount);
            for(int i = 0; i < record.textureCount; i++){
                buf.writeEnum(record.status.get(i));
                buf.writeInt(record.progresses.get(i));
            }
        }

        public static Record readFromBuf(FriendlyByteBuf buf){
            int entityId = buf.readInt();
            int ptr = buf.readInt();
            int count = buf.readInt();
            Record rec = new Record(entityId, ptr, count);
            for(int i = 0; i < count; i++){
                rec.status.add(buf.readEnum(TextureControlCode.class));
                rec.progresses.add(buf.readInt());
            }
            return rec;
        }

        public TextureControlCode getStatus(int i){
            return status.get(i);
        }
        public int getProgress(int i){
            return progresses.get(i);
        }
    }


}
