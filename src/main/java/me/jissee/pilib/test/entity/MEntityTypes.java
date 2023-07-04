package me.jissee.pilib.test.entity;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static me.jissee.pilib.PILib.MODID;

public class MEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);


    public static final RegistryObject<EntityType<TestEntity>> TEST_ENTITY = ENTITY_TYPES.register("testentity",
            ()->EntityType.Builder.of(TestEntity::new, MobCategory.AMBIENT).sized(0.6f,1.8f)//0.6,1.8
                    .build(new ResourceLocation(MODID,"testentity").toString()));

    public static final RegistryObject<EntityType<XYY>> XYY = ENTITY_TYPES.register("xyy",
            ()->EntityType.Builder.of(XYY::new, MobCategory.AMBIENT).sized(0.6f,1.8f)//0.6,1.8
                    .build(new ResourceLocation(MODID,"xyy").toString()));


}
