package me.jissee.pilib.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;

import java.util.ArrayList;

/**
 * 在二维实体内使用，被渲染器调用来管理材质<br/><br/>
 * Used in 2D entities to manage texture sets and called by renderer.
 */
public class Texture2DManager {
    private final ArrayList<Texture2D> textureSets = new ArrayList<>();
    private int texturePtr;
    /**
     * 创建二维材质管理器
     *
     */
    public Texture2DManager(){
        this.texturePtr = 0;
    }

    /**
     * 创建二维材质管理器
     *
     * @param defaultPtr 默认材质下标指针，空则为0；
     */
    public Texture2DManager(int defaultPtr){
        this.texturePtr = defaultPtr;
    }

    /**
     * 按顺序添加二维材质
     * Add texture by sequence.
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
        this.texturePtr = Math.min(texturePtr, textureSets.size() - 1);
        return this.textureSets.get(texturePtr);
    }

    /**
     * 切换二维材质
     * Change texture
     * @param newPtr   新的下标指针 The new index of the textures.
     * @param keepOld  是否保留旧材质的信息，如播放位置 Whether to keep the information like playing position of old texture.
     * @param resetNew 是否重置新材质的信息，如播放位置 Whether to reset the information like playing position of new texture.
     */

    public void changeTextureSet(int newPtr, boolean keepOld, boolean resetNew){
        int oldPtr = texturePtr;
        if((textureSets.get(oldPtr) instanceof VideoResource vRes)){
            SoundInstance si = vRes.getSoundInstance();
            if(si != null){
                Minecraft.getInstance().getSoundManager().stop();
            }

        }else{
            if(!keepOld){
                reset(oldPtr);
            }else{
                resume(oldPtr);
            }
        }
        texturePtr = Math.min(newPtr, textureSets.size() - 1);

        if(textureSets.get(texturePtr) instanceof VideoResource){
            reset(texturePtr);
        }else{
            if(resetNew){
                reset(texturePtr);
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
        textureSets.get(ptr).pause();
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
        textureSets.get(ptr).resume();
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
        textureSets.get(ptr).startOrReset();
    }

    /**
     * 获取当前材质的数量
     */
    public int getTextureCount(){
        return textureSets.size();
    }

    /**
     * 材质播放控制码和状态码
     * Control and status code
     */
    public enum ControlCode{
        NONE,
        CHANGE,
        START_OR_RESET,
        PAUSE,
        RESUME
    }
}
