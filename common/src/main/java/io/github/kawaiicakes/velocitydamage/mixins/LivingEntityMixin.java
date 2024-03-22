package io.github.kawaiicakes.velocitydamage.mixins;

import io.github.kawaiicakes.velocitydamage.config.ConfigValues;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean isFallFlying();

    @Shadow @Final private static Logger LOGGER;
    @Unique
    private Vec3 velocitydamage$movementDeltaPseudoLocal;

    private LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void updateMovementDelta(CallbackInfo ci) {
        this.velocitydamage$movementDeltaPseudoLocal = this.getDeltaMovement();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V", shift = At.Shift.AFTER))
    private void checkCollisionDamage(CallbackInfo ci) {
        if (ConfigValues.getInstance().damageOnAcceleration) {
            // FIXME: collision damage w/ elytra may still be doubled up here
            float rawDamage = (float) (((this.velocitydamage$movementDeltaPseudoLocal.horizontalDistance() - this.getDeltaMovement().horizontalDistance()) * 10.0F) - 3.0F);
            float correctedDamage = rawDamage - ConfigValues.getInstance().accelerationThreshold;
            if (!this.isFallFlying() && this.horizontalCollision && !this.level.isClientSide && correctedDamage > 0.0F) {
                LOGGER.info(String.valueOf(correctedDamage));
                this.playSound(PLAYER_ATTACK_CRIT, Mth.clamp(correctedDamage, 0.5F, 5.0F), 0.7f);
                this.hurt(DamageSource.FLY_INTO_WALL, correctedDamage);
            }
        }
    }
}
