package me.jissee.entityrenderlib2d.test.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static me.jissee.entityrenderlib2d.EntityRenderLib2D.MODID;


public class MSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUNDEVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,MODID);

    public static final RegistryObject<SoundEvent> XYY25 = register("music.xyy25");



    //qwewq
    private static RegistryObject<SoundEvent> register(String id) {
        return SOUNDEVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, id)));
    }
}
