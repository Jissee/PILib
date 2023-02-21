package me.jissee.entityrenderlib2d.test.client.render;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import me.jissee.entityrenderlib2d.render.Renderer2D;
import me.jissee.entityrenderlib2d.test.entity.TestEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class TestEntityRenderer extends Renderer2D<TestEntity> {

    public TestEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.setEntityHeight(1.8F)
                .useRendererSettings(RenderSetting.CENTER_ROTATIONAL_SINGLE)
                .useTexturesSettings()
                .setTextureScale(3.2F, 1.8F);

    }




}
