package io.github.kawaiicakes.velocitydamage.mixins;

import net.minecraft.world.phys.Vec3;

public interface EntityMixinAccess {
    Vec3 velocitydamage$getDeltaMovementO();
    void velocitydamage$setDeltaMovementO(Vec3 deltaMovementO);
}
