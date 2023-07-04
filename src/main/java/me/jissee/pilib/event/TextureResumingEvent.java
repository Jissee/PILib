package me.jissee.pilib.event;

import me.jissee.pilib.resource.Texture2DManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * 当材质即将恢复时触发此事件，此事件可以取消。
 * 此事件发布在{@link MinecraftForge#EVENT_BUS}上。<br/>
 * This event is fired when the texture of the entity is about to resume.
 * This event is cancellable.
 * This event is posted on {@link MinecraftForge#EVENT_BUS}.
 */
public class TextureResumingEvent extends Event {
    private final Texture2DManager manager;
    private final int texturePtr;
    private final int progress;
    public TextureResumingEvent(Texture2DManager manager, int texturePtr, int progress){
        this.manager = manager;
        this.texturePtr = texturePtr;
        this.progress = progress;
    }

    public Texture2DManager getTexture2DManager() {
        return manager;
    }

    public int getTexturePtr() {
        return texturePtr;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
