package me.jissee.pilib.network;

import me.jissee.pilib.render.Renderable2D;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class SoundStopPacket {
    public SoundStopPacket(){
    }
    public SoundStopPacket(FriendlyByteBuf buf){
    }
    public void encode(FriendlyByteBuf buf){
    }
    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            if(FMLLoader.getDist().isClient()){
                Minecraft.getInstance().getSoundManager().stop();
            }
        });
    }
}
