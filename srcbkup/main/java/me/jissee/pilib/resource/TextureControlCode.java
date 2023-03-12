package me.jissee.pilib.resource;

/**
 * 材质播放控制码和状态码
 * Control and status code
 */
public enum TextureControlCode {
    NONE(0),
    START_OR_RESET(1),
    PAUSE(2),
    RESUME(3);
    private final int i;

    TextureControlCode(int i) {
        this.i = i;
    }
    public int getIndex(){
        return i;
    }
}
