package io.github.kawaiicakes.velocitydamage.mixins;

import io.github.kawaiicakes.velocitydamage.api.EntityMixinAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin adds a single field, <code>deltaMovementO</code>, to all <code>Entity</code> instances, as well as a
 * getter and setter. Bear in mind that in vanilla, the <code>ServerPlayer</code>'s delta movement
 * is not updated and will always be <code>Vec3$ZERO</code>.
 * <br><br>
 * The intent is to allow for easy calculation of an entity's approximate acceleration using the difference in
 * the delta of position per tick. This in theory should permit simpler calculation of when deceleration/acceleration
 * damage should be applied, as opposed to checking for collisions as in the previous releases of this mod.
 * <br><br>
 * This mixin is also responsible for updating the value of <code>deltaMovementO</code>. Thanks to this mixin, the
 * utility methods found in {@link io.github.kawaiicakes.velocitydamage.api.VelocityDamageMath} work automagically.
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements EntityMixinAccess {
    @Shadow
    private Vec3 deltaMovement;

    @Unique
    private Vec3 velocitydamage$deltaMovementO = Vec3.ZERO;

    @Override
    public Vec3 velocitydamage$getDeltaMovementO() {
        return this.velocitydamage$deltaMovementO;
    }

    @Override
    public void velocitydamage$setDeltaMovementO(Vec3 deltaMovementO) {
        this.velocitydamage$deltaMovementO = deltaMovementO;
    }

    @Inject(method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "HEAD"))
    public void setDeltaMovement(Vec3 motion, CallbackInfo ci) {
        this.velocitydamage$setDeltaMovementO(this.deltaMovement);
    }
}
