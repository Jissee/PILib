package me.jissee.entityrenderlib2d.resource;

import me.jissee.entityrenderlib2d.render.TexturePosition;
import net.minecraft.resources.ResourceLocation;

public interface Texture2D {
    ResourceLocation getCurrentTexture();
    TexturePosition getCenteredOn();
    boolean isPerpendicular();
    void startOrReset();
    void pause();
    void resume();


}
