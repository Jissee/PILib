package me.jissee.pilib.event;

import me.jissee.pilib.resource.MSoundEngine;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.jissee.pilib.PILib.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onSoundEngineLoading(SoundEngineLoadEvent event){
        SoundEngine soundEngine = event.getEngine();
        MSoundEngine.init(soundEngine);
    }
}
