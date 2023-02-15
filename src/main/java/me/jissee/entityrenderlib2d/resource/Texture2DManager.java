package me.jissee.entityrenderlib2d.resource;

import java.util.ArrayList;

/**
 * 在二维实体内使用，被渲染器调用来管理材质<br/><br/>
 * Used in 2D entities to manage texture sets and called by renderer.
 */
public class Texture2DManager {
    private final ArrayList<Texture2D> textureSets = new ArrayList<>();
    private int texturePtr;
    private boolean needReset;
    public Texture2DManager(){
        this.texturePtr = 0;
    }
    public Texture2DManager(int defaultPtr){
        this.texturePtr = defaultPtr;
    }


    public Texture2DManager addTextureSet(Texture2D textureSet) {
        this.textureSets.add(textureSet);
        return this;
    }

    public Texture2D getTextureSet(){
        return this.textureSets.get(texturePtr);
    }

    public void changeTextureSet(int newPtr){
        this.texturePtr = newPtr;
        resetAnimations();
    }

    public void pause(){
        this.textureSets.get(texturePtr).pause();
    }

    public void resume(){
        this.textureSets.get(texturePtr).resume();
    }

    public void resetAnimations(){
        for (Texture2D group : textureSets) {
            group.startOrReset();
        }
    }

    public void setNeedReset(boolean needReset){
        this.needReset = needReset;
    }

    public int getTextureCount(){
        return textureSets.size();
    }

    public boolean needReset() {
        if(needReset){
            needReset = false;
            return true;
        }
        return false;
    }

    public enum ControlCode{
        NONE,
        CHANGE,
        START_OR_RESET,
        PAUSE,
        RESUME
    }
}
