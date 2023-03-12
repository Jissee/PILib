package me.jissee.pilib.test.entity;

import me.jissee.pilib.render.RenderSetting;
import me.jissee.pilib.render.Renderable2D;
import me.jissee.pilib.resource.*;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.io.File;

import static me.jissee.pilib.PILib.MODID;
import static me.jissee.pilib.resource.ResourceUtil.toNanoInterval;
import static me.jissee.pilib.test.client.MSoundEvents.XYY25;


public class TestEntity extends Mob implements Renderable2D {

    private Player interactingWith;
    private final Texture2DManager manager = new Texture2DManager(this, 0);
    private TextureControlCode textureControlCode = TextureControlCode.NONE;
    private float yrot = 0f;
    public static VideoResource vdo;


    protected TestEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        manager.addTextureSet(prepareAnimation2())
                .addTextureSet(prepareAnimation3())
                .addTextureSet(prepareCombined4())
                .addTextureSet(prepareVideo5());



    }


    public static AttributeSupplier.Builder prepareAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.MAX_HEALTH,20)
                .add(Attributes.JUMP_STRENGTH,1);
    }

    @Override
    public void tick() {
        super.tick();
        if(textureControlCode == TextureControlCode.RESUME){
            yrot += 0.01;
            //if(yrot > 2 * Math.PI) yrot -= 2 * Math.PI;
        }
        this.setYRot(yrot);
        //this.lookAt(EntityAnchorArgument.Anchor.EYES,new Vec3(0,yrot,0));
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        interactingWith = pPlayer;
        int num = pPlayer.getItemInHand(pHand).getCount();
        switch (num){
            case 1:
                manager.change(0,false,-1);
                break;
            case 2:
                manager.change(1,false,-1);
                break;
            case 3:
                manager.change(2,false,-1);
                break;
            case 4:
                manager.change(3,false,-1);
                break;
            case 63:
                manager.pause();
                break;
            case 64:
                manager.resume();
        }
        return super.mobInteract(pPlayer, pHand);
    }

    public Player getInteractingWith() {
        return interactingWith;
    }


    @Override
    public Texture2DManager getTexture2DManager() {
        return manager;
    }



    public Animation2D prepareAnimation2(){
        return Animation2D.createSingleSide(MODID,"%i","textures/entity/pic_%i.png",
                7,14,toNanoInterval(25),10,TextureControlCode.START_OR_RESET,
                new TextureSetting(49f/70f,77f/70f)
                ,RenderSetting.BOTTOM_ROTATIONAL_SINGLE);
    }
    public Animation2D prepareAnimation3(){
        return new Animation2D(-1,toNanoInterval(1),TextureControlCode.START_OR_RESET,
                new TextureSetting(1.92f,1.08f),
                RenderSetting.PERPENDICULAR_SINGLE)
                .addTexture(new ResourceLocation(MODID, "textures/entity/png_t.png"))
                .addTexture(new ResourceLocation(MODID, "textures/entity/png.png"));
    }
    public Animation2D.Combined prepareCombined4(){
        return new Animation2D.Combined(TextureControlCode.START_OR_RESET)
                .add(prepareAnimation2().setRepeat(10))
                .add(prepareAnimation3().setRepeat(10))
                .setRepeat(1);
    }

    public VideoResource prepareVideo5(){
        return new VideoResource(new File(ResourceUtil.getFileName(MODID, 0)), "",1, 25f, 3037, XYY25.get(), true, new TextureSetting(3.2f,1.8f), RenderSetting.CENTER_ROTATIONAL_SINGLE);
    }

    public static VideoResource prepareVideo6(){
        return new VideoResource(new File("I:\\develop\\xyys04e01.mp4"),"s04e01",1,24f, 21049, null, true, new TextureSetting(3.2f,1.8f),RenderSetting.CENTER_ROTATIONAL_SINGLE);
    }
}
