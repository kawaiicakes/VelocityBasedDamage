package io.github.kawaiicakes.velocitydamage.mixins;

import io.github.kawaiicakes.velocitydamage.config.ConfigValues;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
            // FIXME: damage calculation is missing a lot of the locals necessary for accurate values.
            // FIXME: collision damage w/ elytra may still be doubled up here
            // this might actually be fine if the local float damage is doing what I think it is. I could just tweak some values around and then implement functionality for ServerPlayers
            float damage = (float) (((this.velocitydamage$movementDeltaPseudoLocal.horizontalDistance() - this.getDeltaMovement().horizontalDistance()) * 10.0F) - 3.0F);
            if (!this.isFallFlying() && this.horizontalCollision && !this.level.isClientSide && damage > 0.0F) {
                this.playSound(PLAYER_ATTACK_CRIT, Mth.clamp(damage, 1.0F, 5.0F), 0.7f);
                this.hurt(DamageSource.FLY_INTO_WALL, damage);
            }
        }
    }
}
