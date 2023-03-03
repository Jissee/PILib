package me.jissee.pilib.render;

/**
 * 设置实体的旋转以及面数。<br/><br/>
 * Configure the rotation and sides of the entity.<br/><br/>
 */
public class RenderSetting {
    /**
     * 实体的材质会围绕实体的中心点旋转，并且永远面对玩家。在这种设置下，实体只有一个面，如果之后可能切换到双面，你也可以添加双面材质。<br/><br/>
     * The texture will rotate around the center of the entity, and it will always face the player.
     * It has only one side, but you can set textures of double sides in case of changing into double.<br/><br/>
     */
    public static final RenderSetting CENTER_ROTATIONAL_SINGLE = new RenderSetting(TexturePosition.CENTER,false,TextureSide.SINGLE);
    /**
     * 实体的材质会围绕实体的底部点旋转，并且永远面对玩家。在这种设置下，实体只有一个面，如果之后可能切换到双面，你也可以添加双面材质。<br/><br/>
     * The texture will rotate around the bottom of the entity, and it will always face the player.
     * It has only one side, but you can set textures of double sides in case of changing into double.<br/><br/>
     */
    public static final RenderSetting BOTTOM_ROTATIONAL_SINGLE = new RenderSetting(TexturePosition.BOTTOM,false,TextureSide.SINGLE);
    /**
     * 实体的材质垂直于地面，并且永远面对玩家。在这种设置下，实体只有一个面，如果之后可能切换到双面，你也可以添加双面材质。<br/><br/>
     * The texture will be perpendicular to the ground, and it will always face the player.
     * It has only one side, but you can set textures of double sides in case of changing into double.<br/><br/>
     */
    public static final RenderSetting PERPENDICULAR_SINGLE = new RenderSetting(TexturePosition.CENTER, true, TextureSide.SINGLE);
    /**
     * 实体的材质垂直于地面，并且有自己的旋转。在这种设置下，实体有两个面，请添加双面材质并自行设置实体的旋转。<br/><br/>
     * The texture will be perpendicular to the ground， and has its own yaw.
     * It has two sides. You should set the textures of both sides and configure the rotation of the entity by yourself.<br/><br/>
     */
    public static final RenderSetting PERPENDICULAR_DOUBLE = new RenderSetting(TexturePosition.CENTER, true, TextureSide.DOUBLE);

    private final TexturePosition position;
    private final boolean perpendicular;
    private final TextureSide textureSide;

    /**
     * 推荐使用预设<br/>
     * Some pre-set RenderSetting are recommended to use.
     * @param position      实体旋转围绕的中心 Which position will the texture centered on
     * @param perpendicular 实体是否垂直于地面，或者随着玩家视角旋转 Whether the pictures should rotate with the camera or only be perpendicular to the ground
     * @param textureSide   如果实体垂直于地面，它可以具有两个面 If the texture is perpendicular to the ground, it can be two sides. So there will be a front and back.
     */
    public RenderSetting(TexturePosition position, boolean perpendicular, TextureSide textureSide){
        this.position = position;
        this.perpendicular = perpendicular;
        this.textureSide = textureSide;
    }

    public TexturePosition getPosition() {
        return position;
    }


    public boolean isPerpendicular() {
        return perpendicular;
    }

    public TextureSide getTextureSide() {
        return textureSide;
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
