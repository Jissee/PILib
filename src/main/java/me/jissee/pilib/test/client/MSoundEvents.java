package me.jissee.pilib.test.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static me.jissee.pilib.PILib.MODID;


public class MSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDEVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,MODID);

    public static final RegistryObject<SoundEvent> XYY25 = register("music.xyy25");




    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDEVENTS.register(id, () -> new SoundEvent(new ResourceLocation(MODID, id)));
    }
}
