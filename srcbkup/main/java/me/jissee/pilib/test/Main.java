package me.jissee.pilib.test;

import me.jissee.pilib.render.RenderSetting;
import me.jissee.pilib.resource.*;
import me.jissee.pilib.test.client.MSoundEvents;
import me.jissee.pilib.test.entity.MEntityTypes;
import me.jissee.pilib.test.entity.TestEntity;
import net.minecraft.nbt.CompoundTag;
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
        ResourceUtil.extractAll(MODID);
        //TestEntity.vdo = TestEntity.prepareVideo6();

    }

    public static void main(String[] args) {
        System.out.println(123);
        CompoundTag rootTag = new CompoundTag();
        CompoundTag tag = new CompoundTag();

        tag.putInt("key1",1);
        tag.putLong("key2", 123);
        tag.putString("str3", "111");
        rootTag.put("tag",tag);

        CompoundTag newTag = new CompoundTag();
        newTag.putInt("key1",2);
        newTag.putLong("key2", 124);
        newTag.putString("str3", "222");
        rootTag.put("tag",newTag);

        int pausehere = 0;
        /*
        Texture2DManager manager = new Texture2DManager(null);
        manager.addTextureSet(prepareAnimation2())
                .addTextureSet(prepareAnimation3())
                .addTextureSet(prepareCombined4());
        manager.change(2,false,-1);
        Texture2D texture2D= manager.getTextureSet();
        while(true){
            System.out.println(texture2D.getCurrentTextureFront());
            //if(texture2D instanceof Animation2D.Combined com){
            //    Tuple<Integer, Integer> t = com.getCurrentTuple();
            //    System.out.println(t.getA() + " " + t.getB());
            //}

        }*/
    }

    public static Animation2D prepareAnimation2(){
        return Animation2D.createSingleSide(MODID,"%i","textures/entity/pic_%i.png",
                7,14,
                toNanoInterval(25),-1,TextureControlCode.START_OR_RESET,
                new TextureSetting(49f/70f,77f/70f),
                RenderSetting.PERPENDICULAR_SINGLE);
    }
    public static Animation2D prepareAnimation3(){
        return new Animation2D(-1,toNanoInterval(1),TextureControlCode.START_OR_RESET,new TextureSetting(1.92f,1.08f),RenderSetting.PERPENDICULAR_SINGLE)
                .addTexture(new ResourceLocation(MODID, "textures/entity/png_t.png"))
                .addTexture(new ResourceLocation(MODID, "textures/entity/png.png"));
    }
    public static Animation2D.Combined prepareCombined4(){
        return new Animation2D.Combined(TextureControlCode.START_OR_RESET)
                .add(prepareAnimation2().setRepeat(10))
                .add(prepareAnimation3().setRepeat(10))
                .setRepeat(-1);
    }
}
