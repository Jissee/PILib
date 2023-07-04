package me.jissee.pilib.test.entity;

import me.jissee.pilib.render.RenderSetting;
import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.SingleTexture2D;
import me.jissee.pilib.resource.Texture2DManager;
import me.jissee.pilib.resource.TextureSetting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import static me.jissee.pilib.PILib.MODID;

public class XYY extends Animal implements Renderable2D {
    private final Texture2DManager manager = new Texture2DManager(this);
    protected XYY(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        manager.addTextureSet(new SingleTexture2D(new ResourceLocation(MODID,"textures/entity/xyy.png"),
                new ResourceLocation(MODID,"textures/entity/xyy.png"),new TextureSetting(470f/500f,500f/500f), RenderSetting.PERPENDICULAR_DOUBLE));
    }
    public static AttributeSupplier.Builder prepareAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.MAX_HEALTH,20)
                .add(Attributes.JUMP_STRENGTH,1);
    }
    @Override
    public Texture2DManager getTexture2DManager() {
        return manager;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return null;
    }
}
