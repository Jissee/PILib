package me.jissee.pilib.event;

import me.jissee.pilib.resource.Texture2DManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

/**
 * 当视频播放完时触发此事件，此事件不可取消。
 * 此事件发布在{@link MinecraftForge#EVENT_BUS}上。<br/>
 * This event is fired when the video is finished.
 * This event is not cancellable.
 * This event is posted on {@link MinecraftForge#EVENT_BUS}.
 */
public class VideoFinishedPlayingEvent extends Event {
    private final Texture2DManager manager;
    private final int videoPtr;
    public VideoFinishedPlayingEvent(Texture2DManager manager, int videoPtr){
        this.manager = manager;
        this.videoPtr = videoPtr;
    }
    public Texture2DManager getTexture2DManager() {
        return manager;
    }
    public int getVideoPtr(){
        return videoPtr;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
