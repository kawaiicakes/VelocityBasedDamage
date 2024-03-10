package io.github.kawaiicakes.velocitydamage.mixins;

import io.github.kawaiicakes.velocitydamage.api.EntityMixinAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    // FIXME: Absolute acceleration returns as 0 every other tick.
    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOldPosAndRot()V"))
    public void tickNonPassenger(Entity entity, CallbackInfo ci) {
        ((EntityMixinAccess) entity).velocitydamage$setDeltaMovementO(entity.getDeltaMovement());
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setOldPosAndRot()V"))
    public void tickPassenger(Entity ridingEntity, Entity passengerEntity, CallbackInfo ci) {
        ((EntityMixinAccess) passengerEntity).velocitydamage$setDeltaMovementO(passengerEntity.getDeltaMovement());
    }
}
