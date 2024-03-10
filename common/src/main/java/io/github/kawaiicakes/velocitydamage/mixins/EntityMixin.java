package io.github.kawaiicakes.velocitydamage.mixins;

import io.github.kawaiicakes.velocitydamage.api.EntityMixinAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This mixin adds a single field, <code>deltaMovementO</code>, to all <code>Entity</code> instances, as well as a
 * getter and setter. This value is to be updated every tick with the previous tick's <code>deltaMovement</code>.
 * How this is done is loader-dependent. Bear in mind that in vanilla, the <code>ServerPlayer</code>'s delta movement
 * is not updated and will always be <code>Vec3$ZERO</code>.
 * <br><br>
 * The intent is to allow for easy calculation of an entity's approximate acceleration using the difference in
 * the delta of position per tick. This in theory should permit simpler calculation of when deceleration/acceleration
 * damage should be applied, as opposed to checking for collisions as in the previous releases of this mod.
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements EntityMixinAccess {
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

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    public void setDeltaMovementRedirect(Entity instance, Vec3 motion) {
        ((EntityMixinAccess) instance).velocitydamage$setDeltaMovementO(instance.getDeltaMovement());
        instance.setDeltaMovement(motion);
    }
}
