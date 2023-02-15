package me.jissee.entityrenderlib2d.render;


import me.jissee.entityrenderlib2d.resource.Texture2DManager;

/**
 * 任何二维渲染的实体应该实现此接口；<br/>
 * 使用{@link Texture2DManager}来管理材质。<br/><br/>
 * Any entities that need to be rendered by 2D textures should implement this interface.<br/>
 * Create a {@link Texture2DManager} to manager texture sets.<br/><br/>
 */
public interface Renderable2D {
    Texture2DManager getTexture2DManager();

}
