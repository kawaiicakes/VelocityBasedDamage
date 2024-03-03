package io.github.kawaiicakes.velocitydamage.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.kawaiicakes.velocitydamage.VelocityDamage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamageForge {
    public VelocityDamageForge() {
        EventBuses.registerModEventBus(VelocityDamage.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        VelocityDamage.init();
    }
}