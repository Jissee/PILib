package me.jissee.pilib.event;

import me.jissee.pilib.resource.Texture2DManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * 当材质即将切换时触发此事件，此事件可以取消。
 * 此事件发布在{@link MinecraftForge#EVENT_BUS}上。<br/>
 * This event is fired when the texture of the entity is about to change.
 * This event is cancellable.
 * This event is posted on {@link MinecraftForge#EVENT_BUS}.
 */
public class TextureChangingEvent extends Event {
    private final Texture2DManager manager;
    private final int newPtr;
    private final int progress;
    public TextureChangingEvent(Texture2DManager manager, int newPtr, int progress){
        this.manager = manager;
        this.newPtr = newPtr;
        this.progress = progress;
    }

    public Texture2DManager getTexture2DManager() {
        return manager;
    }

    public int getNewPtr() {
        return newPtr;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
