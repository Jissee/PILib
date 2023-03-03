package me.jissee.pilib.test;

import me.jissee.pilib.render.RenderSetting;
import me.jissee.pilib.resource.Animation2D;
import me.jissee.pilib.resource.ResourceUtil;
import me.jissee.pilib.resource.Texture2D;
import me.jissee.pilib.resource.Texture2DManager;
import me.jissee.pilib.test.client.MSoundEvents;
import me.jissee.pilib.test.entity.MEntityTypes;
import me.jissee.pilib.test.entity.TestEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import static me.jissee.pilib.PILib.MODID;
import static me.jissee.pilib.resource.ResourceUtil.toNanoInterval;


/**
 * Support Windows and Mac only.
 */

public class Main
{
    public static void setuptest(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MEntityTypes.ENTITY_TYPES.register(modEventBus);
        MSoundEvents.SOUNDEVENTS.register(modEventBus);

        if(FMLLoader.getDist().isDedicatedServer()){
            modEventBus.addListener(
                    (EntityAttributeCreationEvent e) -> e.put(MEntityTypes.TEST_ENTITY.get(), TestEntity.prepareAttributes().build()));
        }
        ResourceUtil.addExclusion(MODID, "ffmpeg.exe");
        ResourceUtil.register(new ResourceLocation(MODID,"video/qmdyj-full.mp4"));
        //ResourceUtil.extractAll(MODID);
        //TestEntity.vdo = TestEntity.prepareVideo6();

    }

    public static void main(String[] args) {
        System.out.println(123);
        Texture2DManager manager = new Texture2DManager();
        manager.addTextureSet(prepareAnimation2())
                .addTextureSet(prepareAnimation3())
                .addTextureSet(prepareCombined4());
        manager.changeTextureSet(2,false,true);
        Texture2D texture2D= manager.getTextureSet();
        while(true){
            System.out.println(texture2D.getCurrentTextureFront());
            //if(texture2D instanceof Animation2D.Combined com){
            //    Tuple<Integer, Integer> t = com.getCurrentTuple();
            //    System.out.println(t.getA() + " " + t.getB());
            //}

        }
    }

    public static Animation2D prepareAnimation2(){
        return Animation2D.createSingleSide(MODID,"%i","textures/entity/pic_%i.png",7,14,49f/70f,77f/70f, toNanoInterval(25),-1, RenderSetting.PERPENDICULAR_SINGLE);
        //new Animation2D(-1,(long) 1e9, RenderSetting.PERPENDICULAR_DOUBLE);
    }
    public static Animation2D prepareAnimation3(){
        return new Animation2D(-1,toNanoInterval(1),RenderSetting.PERPENDICULAR_SINGLE)
                .addTexture(new ResourceLocation(MODID, "textures/entity/png_t.png"))
                .addTexture(new ResourceLocation(MODID, "textures/entity/png.png"));
    }
    public static Animation2D.Combined prepareCombined4(){
        return new Animation2D.Combined()
                .add(prepareAnimation2().setRepeat(10))
                .add(prepareAnimation3().setRepeat(10))
                .setRepeat(-1);
    }
/*
    private int getIdx(){
        long timeNow = System.nanoTime();
        long diff = timeNow - previousNanoTime;

        assert textureRemainTime <= animRemainTime;
        assert animRemainTime <= totalRemainTime;

        if(textureRemainTime != thisAnim.nanoInterval && diff > textureRemainTime){            //adjust texture
            totalRemainTime -= textureRemainTime;
            animRemainTime -= textureRemainTime;
            diff -= textureRemainTime;
            textureIndex++;
            textureRemainTime = thisAnim.nanoInterval;
        }else{                                   //in texture
            previousNanoTime = timeNow;
            return textureIndex;
        }

        if(animRemainTime != animTimeList.get(animIndex) && diff > animRemainTime){               //adjust animation
            totalRemainTime -= animRemainTime;
            diff -= animRemainTime;
            nextAnimIndex();
            animRemainTime = animTimeList.get(animIndex);
            textureIndex = 0;
        }else{                                   //in animation
            int pass = (int) (diff / thisAnim.nanoInterval);
            textureRemainTime = (pass + 1) * thisAnim.nanoInterval - diff;
            previousNanoTime = timeNow;
            return textureIndex += pass;
        }


        if(diff > totalRemainTime){              //adjust total
            diff -= totalRemainTime;
            repeatCopy--;
            totalRemainTime = totalTime;
            animIndex = 0;
        }else{                                   //in total animations
            long lastsTime;
            while(diff > (lastsTime = animTimeList.get(animIndex))){
                diff -= lastsTime;
                nextAnimIndex();
            }
            thisAnim = animList.get(animIndex);
            animRemainTime = lastsTime - diff;

            int pass = (int) (diff / thisAnim.nanoInterval);
            textureRemainTime = (pass + 1) * thisAnim.nanoInterval - diff;
            previousNanoTime = timeNow;
            return textureIndex += pass;
        }



        while((repeatCopy > 0 || repeat == -1) && diff > totalTime){
            diff -= totalTime;
            repeatCopy--;
        }
        long lastsTime;
        while(diff > (lastsTime = animTimeList.get(animIndex))){
            diff -= lastsTime;
            nextAnimIndex();
        }
        thisAnim = animList.get(animIndex);
        animRemainTime = lastsTime - diff;

        int pass = (int) (diff / thisAnim.nanoInterval);
        textureRemainTime = (pass + 1) * thisAnim.nanoInterval - diff;
        previousNanoTime = timeNow;
        return textureIndex += pass;
    }

 */

}
