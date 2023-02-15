package me.jissee.entityrenderlib2d.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import me.jissee.entityrenderlib2d.resource.Texture2D;
import me.jissee.entityrenderlib2d.resource.Texture2DManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

/**
 * 二维渲染器<br/><br/> 
 * Renderer for 2D entities.<br/><br/>
 */
public abstract class Renderer2D<T extends Entity & Renderable2D> extends EntityRenderer<T> {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final float textureSizeX = 100;
    private static final float textureSizeY = 100;
    private static final float offsetX = -50;
    private static final float offsetY = -50;
    private float textureScaleX = 1;
    private float textureScaleY = 1;
    private float entityHeight = 1;
    private boolean useRendererSettings = false;
    private RenderSetting setting;

    protected Renderer2D(EntityRendererProvider.Context pContext) {
        super(pContext);
    }


    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        pEntityYaw = angleAdjust2PI(pEntityYaw);
        float pEntityNormal = (float) (-pEntityYaw + Math.PI / 2);
        Texture2DManager manager = pEntity.getTexture2DManager();
        Texture2D texture2D = manager.getTextureSet();

        RenderSetting.TexturePosition position;
        boolean perpendicular;
        RenderSetting.TextureSide side;

        if(useRendererSettings){
            position = setting.getPosition();
            perpendicular = setting.isPerpendicular();
            side = setting.getTextureSide();
        }else{
            position = texture2D.getRenderSetting().getPosition();
            perpendicular = texture2D.getRenderSetting().isPerpendicular();
            side = texture2D.getRenderSetting().getTextureSide();
        }

        Quaternion orientation = mc.getEntityRenderDispatcher().cameraOrientation();

        if(perpendicular){
            if(side == RenderSetting.TextureSide.DOUBLE){
                float f = angleAdjustNPPI(pEntityYaw);
                orientation = Quaternion.fromYXZ(f, 0f, 0f);
            }else{
                Vector3f v = orientation.toYXZ();
                orientation = Quaternion.fromYXZ(v.y(), 0f, 0f);
            }
        }

        VertexConsumer builder;

        if(side == RenderSetting.TextureSide.DOUBLE){
            Vec3 posPlayer = mc.player.getPosition(pPartialTick);
            Vec3 posEntity = pEntity.getPosition(pPartialTick);
            Vec2 posDelta = new Vec2((float) (posPlayer.x - posEntity.x), (float) (posPlayer.z - posEntity.z));
            Vec2 normalDirection = new Vec2((float) Math.cos(pEntityNormal), (float) Math.sin(pEntityNormal));
            float angleDot = posDelta.dot(normalDirection);
            if(angleDot > 0){
                builder = pBuffer.getBuffer(RenderType.text(texture2D.getCurrentTextureFront()));
            }else{
                Vector3f vector3f = orientation.toYXZ();
                orientation = Quaternion.fromYXZ((float) (vector3f.y() + Math.PI), 0, 0);
                builder = pBuffer.getBuffer(RenderType.text(texture2D.getCurrentTextureBack()));
            }
        }else{
            builder = pBuffer.getBuffer(RenderType.text(texture2D.getCurrentTextureFront()));
        }

        if(pEntity instanceof LivingEntity pLivingEntity){
            if (pLivingEntity.deathTime > 0) {
                float f = ((float)pLivingEntity.deathTime + pPartialTick - 1.0F) / 20.0F * 1.6F;
                f = Mth.sqrt(f);
                if (f > 1.0F) {
                    f = 1.0F;
                }
                pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(f * this.getFlipDegrees(pEntity)));
            }
        }

        pPoseStack.pushPose();
        if(position == RenderSetting.TexturePosition.BOTTOM){
            pPoseStack.translate(0,0,0);
            pPoseStack.mulPose(orientation);
            pPoseStack.scale(-textureScaleX / textureSizeX,-textureScaleY / textureSizeY,0);

            vertex(builder,pPoseStack, 0            + offsetX,0          ,0,0,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX,0          ,0,1,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX,-textureSizeY ,0,1,0,255,pPackedLight);
            vertex(builder,pPoseStack, 0            + offsetX,-textureSizeY ,0,0,0,255,pPackedLight);

        }else {
            pPoseStack.translate(0,entityHeight / 2,0);
            pPoseStack.mulPose(orientation);
            pPoseStack.scale(-textureScaleX / textureSizeX,-textureScaleY / textureSizeY,0);

            vertex(builder,pPoseStack, 0            + offsetX,textureSizeY + offsetY,0,0,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX,textureSizeY + offsetY,0,1,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX,0            + offsetY,0,1,0,255,pPackedLight);
            vertex(builder,pPoseStack, 0            + offsetX,0            + offsetY,0,0,0,255,pPackedLight);
        }
        pPoseStack.popPose();
    }

    protected boolean shouldShowName(T pEntity) {
        return shouldShowNameLivingEntity(pEntity) && (pEntity.shouldShowName() || pEntity.hasCustomName() && pEntity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    protected boolean shouldShowNameLivingEntity(T pEntity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
        float f = pEntity.isDiscrete() ? 32.0F : 64.0F;
        if (d0 >= (double)(f * f)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            boolean flag = !pEntity.isInvisibleTo(localplayer);
            if (pEntity != localplayer) {
                Team team = pEntity.getTeam();
                Team team1 = localplayer.getTeam();
                if (team != null) {
                    Team.Visibility team$visibility = team.getNameTagVisibility();
                    switch (team$visibility) {
                        case ALWAYS:
                            return flag;
                        case NEVER:
                            return false;
                        case HIDE_FOR_OTHER_TEAMS:
                            return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                        case HIDE_FOR_OWN_TEAM:
                            return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                        default:
                            return true;
                    }
                }
            }

            return Minecraft.renderNames() && pEntity != minecraft.getCameraEntity() && flag && !pEntity.isVehicle();
        }
    }

    /**
     * 渲染器忽视材质原始大小，渲染时会将其拉伸至指定大小（单位：方块）<br/>
     * The renderer will ignore the origin size of the texture and resize it to the assigned size. Unit: block(s)
     * @param x 水平长度 Width
     * @param y 垂直高度 Height
     */
    public Renderer2D<T> setTextureScale(float x, float y){
        textureScaleX = x;
        textureScaleY = y;
        return this;
    }

    /**
     * 设定实体的大小，用来确定实体中心点的位置<br/>
     * Set the height of the entity, which is used to determine the center of the entity.
     */

    public Renderer2D<T> setEntityHeight(float height){
        entityHeight = height;
        return this;
    }

    /**
     * 调用后所有此渲染器渲染的实体均会采用相同的设定<br/>
     * Any entities that rendered by this renderer will use the same setting.
     */
    public Renderer2D<T> useRendererSettings(RenderSetting setting){
        this.setting = setting;
        this.useRendererSettings = true;
        return this;
    }

    /**
     * 调用后所有此渲染器渲染的实体均会采用其二维材质的设定<br/>
     * Any entities that rendered by this renderer will use the settings from its own textures.
     */
    public Renderer2D<T> useTexturesSettings(){
        this.useRendererSettings = false;
        return this;
    }

    protected float getFlipDegrees(T pLivingEntity) {
        return 90.0F;
    }


    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int alpha, int light) {
        PoseStack.Pose entry = matrixStack.last();
        builder.vertex(entry.pose(), x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(entry.normal(), 0F, 0F, -1F)
                .endVertex();
    }


    /**
     * 二维渲染器不能用于非二维实体，请使用{@link Texture2DManager}来管理和获取二维材质<br/>
     * Renderer2D should not be used for non-2D entities.<br/>
     * Use {@link Texture2DManager} instead.
     */
    @Deprecated
    public ResourceLocation getTextureLocation(T pEntity) {
        throw new IllegalStateException("Renderer2D should not be used for non-2D entities");
    }

    /**
     *
     * @return [0, 2π)
     */
    private static float angleAdjust2PI(float f){
        if(f > 2 * Math.PI){
            float delta = (float) (f - 2 * Math.PI);
            double i = Math.floor(delta / 2 / Math.PI);
            return (float) (f - (i + 1) * 2 * Math.PI);
        }else if(f < 0f){
            float delta = 0f - f;
            double i = Math.floor(delta / 2 / Math.PI);
            return (float) (f + (i + 1) * 2 * Math.PI);
        }else{
            return f;
        }
    }

    /**
     *
     * @return [-π, π)
     */
    private static float angleAdjustNPPI(float f){
        return (float) (angleAdjust2PI(f) - Math.PI);
    }


}
