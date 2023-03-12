package me.jissee.pilib.test.client.render;

import me.jissee.pilib.render.Renderer2D;
import me.jissee.pilib.test.entity.XYY;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class XYYRenderer extends Renderer2D<XYY> {
    public XYYRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }
}
