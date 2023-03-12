package me.jissee.pilib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jissee.pilib.resource.Texture2D;
import me.jissee.pilib.resource.Texture2DManager;
import me.jissee.pilib.resource.TextureSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    protected Renderer2D(EntityRendererProvider.Context pContext) {
        super(pContext);
    }


    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);

        Texture2DManager manager = pEntity.getTexture2DManager();
        if(Minecraft.getInstance().isPaused()){
            if(!manager.isPaused()){
                manager.autoPause();
            }
        }else{
            if(manager.isPaused() && manager.isAutoPause()){
                manager.autoResume();
            }
        }
        pEntityYaw = angleAdjust2PI(pEntityYaw);
        float pEntityNormal = (float) (-pEntityYaw + Math.PI / 2);

        Texture2D texture2D = manager.getTextureSet();
        ResourceLocation front = texture2D.getCurrentTextureFront();
        ResourceLocation back = texture2D.getCurrentTextureBack();

        RenderSetting renderSetting = texture2D.getRenderSetting();

        RenderSetting.TexturePosition position = renderSetting.getPosition();
        boolean perpendicular = renderSetting.isPerpendicular();
        RenderSetting.TextureSide side = renderSetting.getTextureSide();

        float entityHeight = pEntity.getBbHeight();

        TextureSetting textureSetting = texture2D.getTextureSetting();
        float sizeX = textureSetting.sizeX();
        float sizeY = textureSetting.sizeY();
        float toffsetX = textureSetting.offsetX() / sizeX * textureSizeX;
        float toffsetY = textureSetting.offsetY() / sizeY * textureSizeY;


        Quaternionf orientation = mc.getEntityRenderDispatcher().cameraOrientation();

        if(perpendicular){
            if(side == RenderSetting.TextureSide.DOUBLE){
                float f = angleAdjustNPPI(pEntityYaw);
                orientation = new Quaternionf();
                orientation.rotateYXZ(f, 0f, 0f);
            }else{
                Vector3f v = new Vector3f();
                v= orientation.getEulerAnglesYXZ(v);
                orientation = new Quaternionf();
                orientation.rotateYXZ(v.y, 0f, 0f);
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
                builder = pBuffer.getBuffer(RenderType.text(front));
            }else{
                orientation.rotateY((float) Math.PI);
                builder = pBuffer.getBuffer(RenderType.text(back));
            }
        }else{
            builder = pBuffer.getBuffer(RenderType.text(front));
        }

        pPoseStack.pushPose();
        if(position == RenderSetting.TexturePosition.BOTTOM){
            pPoseStack.translate(0,0,0);
            pPoseStack.mulPose(orientation);
            pPoseStack.scale(-sizeX / textureSizeX,-sizeY / textureSizeY,0);

            vertex(builder,pPoseStack, 0            + offsetX + toffsetX,0             - toffsetY,0,0,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX + toffsetX,0             - toffsetY,0,1,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX + toffsetX,-textureSizeY - toffsetY,0,1,0,255,pPackedLight);
            vertex(builder,pPoseStack, 0            + offsetX + toffsetX,-textureSizeY - toffsetY,0,0,0,255,pPackedLight);

        }else {
            pPoseStack.translate(0, entityHeight / 2,0);
            pPoseStack.mulPose(orientation);
            pPoseStack.scale(-sizeX / textureSizeX,-sizeY / textureSizeY,0);

            vertex(builder,pPoseStack, 0            + offsetX + toffsetX,textureSizeY + offsetY - toffsetY,0,0,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX + toffsetX,textureSizeY + offsetY - toffsetY,0,1,1,255,pPackedLight);
            vertex(builder,pPoseStack, textureSizeX + offsetX + toffsetX,0            + offsetY - toffsetY,0,1,0,255,pPackedLight);
            vertex(builder,pPoseStack, 0            + offsetX + toffsetX,0            + offsetY - toffsetY,0,0,0,255,pPackedLight);
        }
        pPoseStack.popPose();
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
