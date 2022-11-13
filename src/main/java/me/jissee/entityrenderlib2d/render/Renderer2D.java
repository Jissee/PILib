package me.jissee.entityrenderlib2d.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import me.jissee.entityrenderlib2d.resource.Texture2DManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;

public abstract class Renderer2D<T extends LivingEntity & Renderable2D> extends EntityRenderer<T> {
    protected Renderer2D(EntityRendererProvider.Context pContext) {
        super(pContext);
    }
    private final Minecraft mc = Minecraft.getInstance();
    private static RenderType2D.EntityRenderPosition renderPosition;

    private final float textureSizeX = 100;
    private final float textureSizeY = 100;

    private float scaleX = 1;
    private float scaleY = 1;

    private float entityHeight = 1;

    private float offsetX = -50;
    private float offsetY = -50;


    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
        if(renderPosition == RenderType2D.EntityRenderPosition.BOTTOM){
            renderBottom(pEntity,pEntityYaw,pPartialTick,pPoseStack,pBuffer,pPackedLight);
        }else{
            renderCenter(pEntity,pEntityYaw,pPartialTick,pPoseStack,pBuffer,pPackedLight);
        }
    }


    public void renderCenter(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.translate(0,entityHeight / 2,0);
        pPoseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pPoseStack.scale(-scaleX / textureSizeX,-scaleY / textureSizeY,0);
        Texture2DManager manager = pEntity.getTexture2DManager();
        VertexConsumer builder = pBuffer.getBuffer(RenderType2D.texture2d(manager.getTextureSet()));
        if (pEntity.deathTime > 0) {
            float f = ((float)pEntity.deathTime + pPartialTick - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(f * this.getFlipDegrees(pEntity)));
        }
        vertex(builder,pPoseStack, 0            + offsetX,textureSizeY + offsetY,0,0,1,255,pPackedLight);
        vertex(builder,pPoseStack, textureSizeX + offsetX,textureSizeY + offsetY,0,1,1,255,pPackedLight);
        vertex(builder,pPoseStack, textureSizeX + offsetX,0            + offsetY,0,1,0,255,pPackedLight);
        vertex(builder,pPoseStack, 0            + offsetX,0            + offsetY,0,0,0,255,pPackedLight);




        pPoseStack.popPose();
    }

    public void renderBottom(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        pPoseStack.translate(0,0,0);
        pPoseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pPoseStack.scale(-scaleX / textureSizeX,-scaleY / textureSizeY,0);
        Texture2DManager manager = pEntity.getTexture2DManager();
        VertexConsumer builder = pBuffer.getBuffer(RenderType2D.texture2d(manager.getTextureSet()));
        if (pEntity.deathTime > 0) {
            float f = ((float)pEntity.deathTime + pPartialTick - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(f * this.getFlipDegrees(pEntity)));
        }
        vertex(builder,pPoseStack, 0            + offsetX,0          ,0,0,1,255,pPackedLight);
        vertex(builder,pPoseStack, textureSizeX + offsetX,0          ,0,1,1,255,pPackedLight);
        vertex(builder,pPoseStack, textureSizeX + offsetX,-textureSizeY ,0,1,0,255,pPackedLight);
        vertex(builder,pPoseStack, 0            + offsetX,-textureSizeY ,0,0,0,255,pPackedLight);


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


    public static RenderType2D.EntityRenderPosition getType(){
        return renderPosition;
    }

    public Renderer2D<T> setPosition(RenderType2D.EntityRenderPosition type){
        renderPosition = type;
        return this;
    }

    public Renderer2D<T> setScale(float x, float y){
        scaleX = x;
        scaleY = y;
        return this;
    }

    public Renderer2D<T> setEntityHeight(float height){
        entityHeight = height;
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
        throw new RuntimeException("Renderer2D should not be used for non-2D entities");
    }

}
