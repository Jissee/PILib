package me.jissee.pilib.test;

import me.jissee.pilib.render.RenderSetting;
import me.jissee.pilib.resource.*;
import me.jissee.pilib.test.client.MSoundEvents;
import me.jissee.pilib.test.entity.MEntityTypes;
import me.jissee.pilib.test.event.TestServerEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.FileNotFoundException;

import static me.jissee.pilib.PILib.MODID;
import static me.jissee.pilib.resource.LocalResourceUtil.toNanoInterval;


/**
 * Support Windows and Mac only.
 */

public class Main
{
    public static final LocalResourceUtil UTIL = new LocalResourceUtil(MODID,false,false);
    public static VideoResource vdo;

    public static void setuptest(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MEntityTypes.ENTITY_TYPES.register(modEventBus);
        MSoundEvents.SOUNDEVENTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(TestServerEventHandler::onVideoFinished);
        if(FMLLoader.getDist().isDedicatedServer()){
            TestServerEventHandler.init(modEventBus);
        }

        //UTIL.addRemovalExclusion(new ResourceLocation(MODID,"video/qmdyj-full.mp4"));
        UTIL.register(new ResourceLocation(MODID,"video/qmdyj-full.mp4"));
        UTIL.extractAll();
        LocalVideoFile f = new LocalVideoFile(UTIL, UTIL.getLocalFileName(0), "", 3037, 25);
        try {
            f.decode();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        vdo  = new VideoResource(f,null, TextureControlCode.PLAYING,  new TextureSetting(3.2f,1.8f), RenderSetting.CENTER_ROTATIONAL_SINGLE);

    }

    public static void main(String[] args) {


    }

    public static Animation2D prepareAnimation2(){
        return Animation2D.createSingleSide(MODID,"%i","textures/entity/pic_%i.png",
                7,14,
                toNanoInterval(25),-1,TextureControlCode.PLAYING,
                new TextureSetting(49f/70f,77f/70f),
                RenderSetting.PERPENDICULAR_SINGLE);
    }
    public static Animation2D prepareAnimation3(){
        return new Animation2D(-1,toNanoInterval(1),TextureControlCode.PLAYING,new TextureSetting(1.92f,1.08f),RenderSetting.PERPENDICULAR_SINGLE)
                .addTexture(new ResourceLocation(MODID, "textures/entity/png_t.png"))
                .addTexture(new ResourceLocation(MODID, "textures/entity/png.png"));
    }
    public static Animation2D.Combined prepareCombined4(){
        return new Animation2D.Combined(TextureControlCode.PLAYING)
                .add(prepareAnimation2().setRepeat(10))
                .add(prepareAnimation3().setRepeat(10))
                .setRepeat(-1);
    }
}
