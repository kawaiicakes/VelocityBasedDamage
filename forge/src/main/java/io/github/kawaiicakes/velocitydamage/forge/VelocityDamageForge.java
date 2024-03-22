package io.github.kawaiicakes.velocitydamage.forge;

import io.github.kawaiicakes.velocitydamage.VelocityDamage;
import net.minecraftforge.fml.common.Mod;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamageForge {
    public VelocityDamageForge() {
        VelocityDamage.init();
    }
}