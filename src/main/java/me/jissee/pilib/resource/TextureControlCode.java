package me.jissee.pilib.resource;

/**
 * 材质播放控制码和状态码
 * Control and status code
 */
public enum TextureControlCode {
    NONE(0),
    PLAYING(1), //status
    PAUSE(2),   //status and control
    RESUME(3),  //control
    CHANGE(4);  //control
    private final int i;

    TextureControlCode(int i) {
        this.i = i;
    }
    public int getIndex(){
        return i;
    }
}
