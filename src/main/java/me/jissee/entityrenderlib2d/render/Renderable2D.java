package me.jissee.entityrenderlib2d.render;


import me.jissee.entityrenderlib2d.resource.Texture2DManager;

/**
 * Any entities that need to be rendered in 2D texture should implement this interface<br\>
 *
 * Register a {@link Texture2DManager} to manager texture sets.
 *
 */
public interface Renderable2D {
    Texture2DManager getTexture2DManager();

}
