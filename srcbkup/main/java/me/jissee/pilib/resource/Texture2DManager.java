package me.jissee.pilib.resource;

import me.jissee.pilib.network.DataRenewPacket;
import me.jissee.pilib.network.DataSyncPacket;
import me.jissee.pilib.network.NetworkHandler;
import me.jissee.pilib.network.SyncAnimData;
import me.jissee.pilib.render.Renderable2D;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

/**
 * 在二维实体内使用，被渲染器调用来管理材质<br/><br/>
 * Used in 2D entities to manage texture sets and called by renderer.
 */
public class Texture2DManager implements Iterable<Texture2D> {
    //private static final HashMap<Integer, Texture2DManager> managers = new HashMap<>();
    private static final ArrayList<Texture2DManager> managers = new ArrayList<>();
    //private static int id = 0;
    private final ArrayList<Texture2D> textureSets = new ArrayList<>();
    private int texturePtr;
    private final Entity target;


    /**
     * 用于客户端接收新数据
     * Client accept new data from server
     */
    public static void onRenewData(SyncAnimData data){
        Texture2DManager manager;
        int lastIndex = -1;
        for(int i = managers.size() - 1; i >= 0; i--){
            manager = managers.get(i);
            if(manager.target.getId() == data.entityId()){
                if(lastIndex == -1){
                    lastIndex = i;
                    manager.onSyncData(data,false);
                }else{
                    managers.remove(i);
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
     * @param keepOld     是否保留旧材质的信息，如播放位置 Whether to keep the information like playing position of old texture.
     * @param progress    新材质的播放进度 The playing time of the new texture.
     */
    public void change(int newPtr, boolean keepOld, int progress){
        if(target.level.isClientSide){
            SyncAnimData data = new SyncAnimData(target.getId(),newPtr,TextureControlCode.START_OR_RESET, progress);
            DataSyncPacket packet = new DataSyncPacket(Minecraft.getInstance().player.getUUID(), data, keepOld);
            NetworkHandler.INSTANCE.sendToServer(packet);
        }
        changeInternal(newPtr, keepOld, progress);
    }
    private synchronized void changeInternal(int newPtr, boolean keepOld, int progress){
        int oldPtr = texturePtr;
        if((textureSets.get(oldPtr) instanceof VideoResource vRes)){
            if(target.level.isClientSide){
                Minecraft.getInstance().getSoundManager().stop();
            }
        }else{
            if(!keepOld){
                textureSets.get(oldPtr).startOrReset();
            }else{
                textureSets.get(oldPtr).pause();
            }
        }
        texturePtr = Math.min(newPtr, textureSets.size() - 1);
        Texture2D newTexture = textureSets.get(texturePtr);

        newTexture.startOrReset();
        newTexture.setProgress(progress);
        if(newTexture instanceof VideoResource videoResource){
            if(target.level.isClientSide){
                Vec3 pos = new Vec3(target.getX(), target.getY(), target.getZ());
                MSoundEngine.playWithProgressOffset(videoResource, pos, progress);
            }
        }


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
        pauseInternal(ptr,textureSets.get(ptr).getProgress());
    }
    private synchronized void pauseInternal(int ptr, int progress){
        Texture2D t = textureSets.get(ptr);
        t.setProgress(progress);
        t.pause();
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
        resumeInternal(ptr,textureSets.get(ptr).getProgress());
    }
    private synchronized void resumeInternal(int ptr, int progress){
        Texture2D t = textureSets.get(ptr);
        t.setProgress(progress);
        t.resume();
    }

    /**
     * 重置当前正在播放的材质
     * Reset the current texture
     */
    public void reset(){
        reset(texturePtr);
    }
    /**
     * 重置指定下标的材质
     * Reset the texture with index
     */
    public void reset(int ptr){
        if(target.level.isClientSide){
            SyncAnimData data = new SyncAnimData(target.getId(),ptr,TextureControlCode.START_OR_RESET, -1);
            DataSyncPacket packet = new DataSyncPacket(Minecraft.getInstance().player.getUUID(),data);
            NetworkHandler.INSTANCE.sendToServer(packet);
        }
        changeInternal(ptr, false, -1);
    }

    /**
     * 用于服务端同步数据
     * Used to synchronize data from the server side
     */
    public synchronized void onSyncData(SyncAnimData data, boolean keepOld){
        assert target.getId() == data.entityId();
        int ptr = data.ptr();
        TextureControlCode code = data.code();
        int progress = data.progress();

        switch (code){
            case START_OR_RESET -> changeInternal(ptr, keepOld, progress);
            case PAUSE -> pauseInternal(ptr, progress);
            case RESUME -> resumeInternal(ptr, progress);
        }
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
    /*
    //managerdata

        "pilib":{
            "count":<count>,
            "ptr":<ptr>,
            "status":[],
            "time":[]
        }
     */



}
