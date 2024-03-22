package io.github.kawaiicakes.velocitydamage.mixins;

import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.velocitydamage.api.VelocityDamageMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Shadow
    private Entity cachedOwner;

    // Fixing via context actions actually causes an InvocationTargetException and failure to inject. I'll just suppress the red squiggles lol
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void inheritMomentumOfOwner(double x, double y, double z, float velocity, float inaccuracy, CallbackInfo ci, Vec3 vec3) {
        if (this.cachedOwner != null) {
            // FIXME: doesn't seem to work... hm... this can probably be fixed when taking the velocity of a ServerPlayer is finished being implemented
            vec3.add(VelocityDamageMath.velocity(this.cachedOwner));
        }
    }
}
