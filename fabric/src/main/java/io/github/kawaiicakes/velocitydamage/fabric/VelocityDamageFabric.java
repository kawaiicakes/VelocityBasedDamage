package io.github.kawaiicakes.velocitydamage.fabric;

import io.github.kawaiicakes.velocitydamage.VelocityDamage;
import net.fabricmc.api.ModInitializer;

public class VelocityDamageFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        VelocityDamage.init();
    }
}
