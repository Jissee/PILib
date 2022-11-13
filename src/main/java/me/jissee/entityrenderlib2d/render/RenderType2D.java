package me.jissee.entityrenderlib2d.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.jissee.entityrenderlib2d.resource.Texture2D;
import net.minecraft.client.renderer.RenderType;

public class RenderType2D extends RenderType {

    public RenderType2D(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }
    public static RenderType texture2d(Texture2D texture2D){
        return RenderType.text(texture2D.getCurrentTexture());
    }
    public enum EntityRenderPosition {
        BOTTOM,
        CENTER

    }
}
