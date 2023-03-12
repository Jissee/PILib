package me.jissee.pilib.test.client.render;

import me.jissee.pilib.render.RenderSetting;
import me.jissee.pilib.render.Renderer2D;
import me.jissee.pilib.test.entity.TestEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class TestEntityRenderer extends Renderer2D<TestEntity> {

    public TestEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }




}
