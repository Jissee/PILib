package me.jissee.entityrenderlib2d.test.entity;

import me.jissee.entityrenderlib2d.render.RenderSetting;
import me.jissee.entityrenderlib2d.render.Renderable2D;
import me.jissee.entityrenderlib2d.resource.Animation2D;
import me.jissee.entityrenderlib2d.resource.ResourceUtil;
import me.jissee.entityrenderlib2d.resource.Texture2DManager;
import me.jissee.entityrenderlib2d.resource.VideoResource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.io.File;

import static me.jissee.entityrenderlib2d.EntityRenderLib2D.MODID;
import static me.jissee.entityrenderlib2d.test.client.MSoundEvents.XYY25;


public class TestEntity extends Mob implements Renderable2D {

    private Player interactingWith;
    private Texture2DManager manager = new Texture2DManager(0);

    private Texture2DManager.ControlCode controlCode = Texture2DManager.ControlCode.NONE;
    private float yrot = 0f;


    protected TestEntity(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        manager.addTextureSet(prepareAnimation2())
                .addTextureSet(prepareAnimation3())
                .addTextureSet(prepare4())
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
        if(controlCode == Texture2DManager.ControlCode.RESUME){
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
                manager.changeTextureSet(3);
                if(!this.level.isClientSide){
                    //playSoundServer(pPlayer,this.getX(),this.getY(),this.getZ(),XYY25.get(),getSoundSource(),1.0f,1.0f, RandomSource.createThreadSafe().nextLong());
                    playSound(XYY25.get());
                }else{
                    //ssi = playSoundClient(pPlayer,this.getX(),this.getY(),this.getZ(),XYY25.get(),getSoundSource(),1.0f,1.0f, RandomSource.createThreadSafe().nextLong());
                    Minecraft.getInstance().getSoundManager().stop();
                }
                break;
            case 2:
                controlCode = Texture2DManager.ControlCode.RESUME;
                /*
                manager.pause();
                if(this.level.isClientSide){
                    Minecraft.getInstance().getSoundManager().pause();
                }
                */
                break;
            case 3:
                controlCode = Texture2DManager.ControlCode.PAUSE;
                /*
                manager.resume();
                if(this.level.isClientSide){
                    Minecraft.getInstance().getSoundManager().resume();
                }
                */
                break;
            case 4:
                manager.changeTextureSet(2);
        }
        //playSound(XYY25.get());



        return super.mobInteract(pPlayer, pHand);
    }

    public Player getInteractingWith() {
        return interactingWith;
    }
/*
    public void playSoundServer(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSoundEvent, SoundSource pSoundSource, float pVolume, float pPitch, long pSeed){
        net.minecraftforge.event.PlayLevelSoundEvent.AtPosition event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtPosition(this.level, pX, pY, pZ, pSoundEvent, pSoundSource, pVolume, pPitch);
        if (event.isCanceled() || event.getSound() == null) return;
        pSoundEvent = event.getSound();
        pSoundSource = event.getSource();
        pVolume = event.getNewVolume();
        pPitch = event.getNewPitch();
        this.getServer().getPlayerList().broadcast(pPlayer, pX, pY, pZ, (double)pSoundEvent.getRange(pVolume), this.level.dimension(), new ClientboundSoundPacket(pSoundEvent, pSoundSource, pX, pY, pZ, pVolume, pPitch, pSeed));
    }
*/

    @Override
    public Texture2DManager getTexture2DManager() {
        return manager;
    }



    public Animation2D prepareAnimation2(){
        return new Animation2D(-1,(long) 1e9, RenderSetting.PERPENDICULAR_DOUBLE)
                .addTexture(new ResourceLocation(MODID,"textures/entity/testentity.png"), new ResourceLocation(MODID,"textures/entity/testentitythin.png"))
                .addTexture(new ResourceLocation(MODID,"textures/entity/transparent.png"), new ResourceLocation(MODID,"textures/entity/testentitythin.png"));
    }

    public Animation2D prepareAnimation3(){
        return new Animation2D(-1,41666666L, RenderSetting.PERPENDICULAR_SINGLE)
                .addTexture(new ResourceLocation(MODID,"textures/entity/testentity.png"))
                .addTexture(new ResourceLocation(MODID,"textures/entity/transparent.png"))
                .addTexture(new ResourceLocation(MODID,"textures/entity/testentitythin.png"));

    }

    public Animation2D prepare4(){
        Animation2D cmb1 = Animation2D.combine(prepareAnimation2(), prepareAnimation3(),-1, prepareAnimation2().getRenderSetting());
        return Animation2D.combine(cmb1, prepareAnimation3(),-1, prepareAnimation2().getRenderSetting());
        //return Animation2D.create(MODID,"%d","textures/entity/out_%d.png",1,3037,40000000L, 1);
    }

    public VideoResource prepareVideo5(){
         return new VideoResource(new File(ResourceUtil.getFileName(MODID, 0)), "", 25,true, RenderSetting.CENTER_ROTATIONAL_SINGLE);
    }
}
