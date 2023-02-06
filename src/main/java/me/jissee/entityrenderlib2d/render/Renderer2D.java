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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;

public abstract class Renderer2D<T extends Entity & Renderable2D> extends EntityRenderer<T> {
    protected Renderer2D(EntityRendererProvider.Context pContext) {
        super(pContext);
    }
    private static final Minecraft mc = Minecraft.getInstance();

    private static final float textureSizeX = 100;
    private static final float textureSizeY = 100;
    private static final float offsetX = -50;
    private static final float offsetY = -50;
    private float textureScaleX = 1;
    private float textureScaleY = 1;
    private float entityHeight = 1;
    private boolean useRendererSettings = false;
    private TexturePosition rendererPosition;
    private boolean rendererPerpendicular;




    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        Texture2DManager manager = pEntity.getTexture2DManager();
        Texture2D texture2D = manager.getTextureSet();

        TexturePosition position;
        boolean perpendicular;

        if(useRendererSettings){
            position = rendererPosition;
            perpendicular = rendererPerpendicular;
        }else{
            position = texture2D.getCenteredOn();
            perpendicular = texture2D.isPerpendicular();
        }

        Quaternion orientation = mc.getEntityRenderDispatcher().cameraOrientation();

        if(perpendicular){
            Vector3f v = orientation.toYXZ();
            orientation = Quaternion.fromYXZ(v.y(), 0f, 0f);
        }

        pPoseStack.pushPose();
        if(position == TexturePosition.BOTTOM){
            pPoseStack.translate(0,0,0);
        }else {
            pPoseStack.translate(0,entityHeight / 2,0);
        }
        pPoseStack.mulPose(orientation);
        pPoseStack.scale(-textureScaleX / textureSizeX,-textureScaleY / textureSizeY,0);

        VertexConsumer builder = pBuffer.getBuffer(RenderType2D.texture2d(texture2D));

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

        if(position == TexturePosition.BOTTOM){
            vertex(builder,pPoseStack, 0            + offsetX,0          ,0,0,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX,0          ,0,1,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX,-textureSizeY ,0,1,0,255,pPackedLight);
            vertex(builder,pPoseStack, 0            + offsetX,-textureSizeY ,0,0,0,255,pPackedLight);
        }else{
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

    public Renderer2D<T> setTextureScale(float x, float y){
        textureScaleX = x;
        textureScaleY = y;
        return this;
    }

    public Renderer2D<T> setEntityHeight(float height){
        entityHeight = height;
        return this;
    }

    public Renderer2D<T> useRendererProperties(TexturePosition position, boolean perpendicular){
        this.rendererPosition = position;
        this.rendererPerpendicular = perpendicular;
        this.useRendererSettings = true;
        return this;
    }

    public Renderer2D<T> useTexturesProperties(){
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
     * Renderer2D should not be used for non-2D entities.<br/>
     * Use {@link Texture2DManager} instead.
     */
    @Deprecated
    public ResourceLocation getTextureLocation(T pEntity) {
        throw new IllegalStateException("Renderer2D should not be used for non-2D entities");
    }

}
