package me.jissee.pilib.render;

import me.jissee.pilib.resource.Texture2DManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * 任何二维渲染的实体应该实现此接口；<br/>
 * Any entities that need to be rendered by 2D textures should implement this interface.<br/>
 */
public interface Renderable2D {
    /**
     * 使用{@link Texture2DManager}来管理材质。<br/>
     * Create a {@link Texture2DManager} to manager texture sets.<br/>
     */
    Texture2DManager getTexture2DManager();

    default void loadData(){
        if(this instanceof Entity entity){
            Texture2DManager manager = this.getTexture2DManager();
            if(manager.getEntityId() == entity.getId()){
                if(!entity.level.isClientSide){
                    CompoundTag entityTag = entity.getPersistentData();
                    //entityTag.remove("pilib");
                    manager.setSaveData(entityTag.getCompound("pilib"));
                }
            }else{
                throw new RuntimeException("Error while loading entity data for " + entity);
            }
        }else{
            throw new RuntimeException("Renderable2D should be implemented on Entity.");
        }
    }

    default void saveData(){
        if(this instanceof Entity entity){
            Texture2DManager manager = this.getTexture2DManager();
            if(manager.getEntityId() == entity.getId()){
                if(!entity.level.isClientSide){
                    CompoundTag entityTag = entity.getPersistentData();
                    entityTag.put("pilib", manager.getSaveData());
                    //entityTag.remove("pilib");
                }
            }else{
                throw new RuntimeException("Error while saving entity data for " + entity);
            }
        }else{
            throw new RuntimeException("Renderable2D should be implemented on Entity.");
        }
    }
}
