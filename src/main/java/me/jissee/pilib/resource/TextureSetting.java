package me.jissee.pilib.resource;

import java.util.Objects;

/**
 * 材质的相关属性
 */
public final class TextureSetting {
    private final float sizeX;
    private final float sizeY;
    private final float offsetX;
    private final float offsetY;

    /**
     * 材质设置<br/>
     * 渲染器忽略材质的原始尺寸，会将其拉伸至指定大小<br/>
     * The renderer ignores the original size of the texture and will resize it to the assigned size.
     * @param sizeX   X尺寸
     * @param sizeY   Y尺寸
     */
    public TextureSetting(float sizeX, float sizeY) {
        this(sizeX, sizeY, 0f, 0f);
    }
    /**
     * 材质设置<br/>
     * 渲染器忽略材质的原始尺寸，会将其拉伸至指定大小<br/>
     * The renderer ignores the original size of the texture and will resize it to the assigned size.
     * @param sizeX   X尺寸
     * @param sizeY   Y尺寸
     * @param offsetX 在X方向上的偏移量
     * @param offsetY 在Y方向上的偏移量
     */
    public TextureSetting(float sizeX, float sizeY, float offsetX, float offsetY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float sizeX() {
        return sizeX;
    }

    public float sizeY() {
        return sizeY;
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextureSetting) obj;
        return Float.floatToIntBits(this.sizeX) == Float.floatToIntBits(that.sizeX) &&
                Float.floatToIntBits(this.sizeY) == Float.floatToIntBits(that.sizeY) &&
                Float.floatToIntBits(this.offsetX) == Float.floatToIntBits(that.offsetX) &&
                Float.floatToIntBits(this.offsetY) == Float.floatToIntBits(that.offsetY);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sizeX, sizeY, offsetX, offsetY);
    }

    @Override
    public String toString() {
        return "TextureSetting[" +
                "sizeX=" + sizeX + ", " +
                "sizeY=" + sizeY + ", " +
                "offsetX=" + offsetX + ", " +
                "offsetY=" + offsetY + ']';
    }


}
