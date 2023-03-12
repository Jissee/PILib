package me.jissee.pilib.resource;

import me.jissee.pilib.network.DataRenewPacket;
import me.jissee.pilib.network.DataSyncPacket;
import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.network.SyncAnimData;
import me.jissee.pilib.render.Renderable2D;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 在二维实体内使用，被渲染器调用来管理材质<br/><br/>
 * Used in 2D entities to manage texture sets and called by renderer.
 */
public class Texture2DManager implements Iterable<Texture2D> {
    private static final ArrayList<Texture2DManager> managers = new ArrayList<>();
    private final ArrayList<Texture2D> textureSets = new ArrayList<>();
    private int texturePtr;
    private final Entity target;
    private boolean autoPause;

    /**
     * 用于客户端接收新数据
     * Client accept new data from server
     */
    public static void onRenewData(DataRenewPacket packet){
        HashMap<Integer, SyncAnimData> map = new HashMap<>();
        for(SyncAnimData data : packet){
            map.put(data.entityId(), data);
        }
        SyncAnimData data;
        Texture2D t;

        for(Texture2DManager manager : managers){
            data = map.get(manager.getEntityId());
            if(data != null){
                manager.texturePtr = data.ptr();
                t = manager.textureSets.get(data.ptr());
                t.setStatusCode(data.code());
                t.setProgress(data.progress());
                if(t instanceof VideoResource videoResource){
                    if(manager.target.level.isClientSide){
                        if(data.code() == TextureControlCode.PLAYING){
                            Vec3 pos = new Vec3(manager.target.getX(), manager.target.getY(), manager.target.getZ());
                            MSoundEngine.playWithProgressOffset(videoResource, pos, data.progress());
                        }
                    }
                }
            }
        }
    }

    public static DataRenewPacket getRenewData(){
        return new DataRenewPacket(managers);
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
            for(int i = 0; i < count; i++){
                TextureControlCode code = TextureControlCode.values()[status[i]];
                textureSets.get(i).setStatusCode(code);
                textureSets.get(i).setProgress(progress[i]);
            }
        }
    }
    public static void tickAll(){
        for(Texture2DManager manager : managers){
            for(Texture2D texture2D : manager){
                texture2D.tick();
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
        managers.add(this);

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
    public void change(int newPtr,  int progress){
        if(target.level.isClientSide){
            SyncAnimData data = new SyncAnimData(target.getId(),newPtr,TextureControlCode.CHANGE, progress);
            DataSyncPacket packet = new DataSyncPacket(Minecraft.getInstance().player.getUUID(), data);
            NetworkHandler.INSTANCE.sendToServer(packet);
        }
        changeInternal(newPtr, progress);
    }
    private synchronized void changeInternal(int newPtr, int progress){
        Texture2D oldt = textureSets.get(texturePtr);
        if(oldt instanceof VideoResource videoResource){
            if(videoResource.getStatusCode() == TextureControlCode.PLAYING){
                if(target.level.isClientSide){
                    MSoundEngine.stopVideoSound(videoResource);
                }
            }
        }
        texturePtr = Math.min(newPtr, textureSets.size() - 1);
        Texture2D t = textureSets.get(texturePtr);

        if(t instanceof VideoResource videoResource){
            if(videoResource.getStatusCode() == TextureControlCode.PLAYING){
                if(target.level.isClientSide){
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
        resume();
        autoPause = false;
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
        if(target.level.isClientSide){
            SyncAnimData data = new SyncAnimData(target.getId(),ptr,TextureControlCode.PAUSE, textureSets.get(ptr).getProgress());
            DataSyncPacket packet = new DataSyncPacket(Minecraft.getInstance().player.getUUID(),data);
            NetworkHandler.INSTANCE.sendToServer(packet);
        }
        pauseInternal(ptr, textureSets.get(ptr).getProgress());
    }
    private synchronized void pauseInternal(int ptr,int progress){
        texturePtr = ptr;
        Texture2D t = textureSets.get(ptr);
        if(t instanceof VideoResource videoResource){
            if(target.level.isClientSide){
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
        if(target.level.isClientSide){
            SyncAnimData data = new SyncAnimData(target.getId(),ptr,TextureControlCode.RESUME, textureSets.get(ptr).getProgress());
            DataSyncPacket packet = new DataSyncPacket(Minecraft.getInstance().player.getUUID(),data);
            NetworkHandler.INSTANCE.sendToServer(packet);
        }
        resumeInternal(ptr, textureSets.get(ptr).getProgress());
    }
    private synchronized void resumeInternal(int ptr, int progress){
        texturePtr = ptr;
        Texture2D t = textureSets.get(ptr);
        if(t instanceof VideoResource videoResource){
            if(target.level.isClientSide){
                Vec3 pos = new Vec3(target.getX(), target.getY(), target.getZ());
                MSoundEngine.playWithProgressOffset(videoResource, pos, progress);
            }
        }
        t.resume();
        t.setProgress(progress);
    }

    /**
     * 用于服务端同步数据
     * Used to synchronize data from the server side
     */
    public synchronized void onSyncData(SyncAnimData data){
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

    public static void clearTextureManagers(){
        managers.clear();
    }


}
