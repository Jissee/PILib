package me.jissee.entityrenderlib2d.render;

public class RenderSetting {
    private TexturePosition position;
    private boolean perpendicular;
    private TextureSide textureSide;

    /**
     *
     * @param position      Which position will the texture centered on
     * @param perpendicular Whether the pictures should rotate with the camera or only be perpendicular to the ground
     * @param textureSide   If the texture is perpendicular to the ground, it can be two sides. So there will be a front and back.
     */
    public RenderSetting(TexturePosition position, boolean perpendicular, TextureSide textureSide){
        this.position = position;
        this.perpendicular = perpendicular;
        this.textureSide = textureSide;
    }

    public TexturePosition getPosition() {
        return position;
    }

    public void setPosition(TexturePosition position) {
        this.position = position;
    }

    public boolean isPerpendicular() {
        return perpendicular;
    }

    public void setPerpendicular(boolean perpendicular) {
        this.perpendicular = perpendicular;
    }

    public TextureSide getTextureSide() {
        return textureSide;
    }

    public void setTextureSide(TextureSide textureSide) {
        this.textureSide = textureSide;
    }

    public enum TexturePosition {
        BOTTOM,
        CENTER
    }

    public enum TextureSide{
        SINGLE,
        DOUBLE
    }
}
