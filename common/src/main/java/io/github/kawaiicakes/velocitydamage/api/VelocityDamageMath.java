package io.github.kawaiicakes.velocitydamage.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Utility-class containing static methods for easily calculating things related to the function of this mod.
 */
public class VelocityDamageMath {

    /**
     * For some reason entities on the ground still have a delta Y change of this value every tick.
     */
    public static final double RESTING_Y_DELTA = -0.0784000015258789;

    /**
     * Returns the <code>Vec3</code> velocity of the passed <code>Entity</code>. Automatically accounts for the
     * resting Y-delta.
     */
    public static Vec3 velocity(Entity entity) {
        return entity.getDeltaMovement().add(0, -RESTING_Y_DELTA, 0);
    }

    /**
     * Positive values indicate that the attacker is approaching the target. Negative indicates that the attacker is
     * retreating from the target.
     * <br><br>
     * Edge cases where the return is not what we would intuit it to be
     * (e.g. attacking on the upswing of a jump into someone is considered retreating, and would therefore
     * theoretically do less damage) are accounted for by substituting attacker/target positions in calculations
     * for their eye positions as needed.
     * @return a <code>double</code> representing a one-dimensional velocity in meters per tick.
     */
    public static double approachVelocity(Entity attacker, LivingEntity target) {
        Vec3 attackerVelocity = velocity(attacker);
        Vec3 targetVelocity = velocity(target);

        if (attackerVelocity.length() == 0 && targetVelocity.length() == 0) return 0;

        Vec3 attackerPosition =
                targetVelocity.y() - attackerVelocity.y() >= 0 && target.position().y() > attacker.position().y() ?
                        attacker.getEyePosition() : attacker.position();
        Vec3 targetPosition =
                targetVelocity.y() - attackerVelocity.y() <= 0 && target.position().y() < attacker.position().y() ?
                        target.getEyePosition() : target.position();

        Vec3 velocityDifference = attackerVelocity.subtract(targetVelocity);
        Vec3 directionToTarget = targetPosition.subtract(attackerPosition).normalize();

        return directionToTarget.dot(velocityDifference);
    }

    /**
     * Looks at the absolute change in speed (not velocity) of the passed <code>Entity</code> over a tick. The direction
     * of acceleration does not matter in the context in which this method is used; we are merely looking for violent
     * changes in acceleration to determine how much, if any, damage should be applied.
     * As to usage, this method theoretically works so long as it's not called before the loader-specific call to
     * {@link EntityMixinAccess#velocitydamage$setDeltaMovementO(Vec3)} is
     * made. I'll have to be careful of that.
     * @return The approximate one-dimensional acceleration, in meters per tick per tick, of the passed
     *          <code>Entity</code> as measured over one tick.
     */
    public static double accelerationAbs(Entity entity) {
        return acceleration(entity).length();
    }

    /**
     * This method theoretically works so long as it's not called before the loader-specific call to
     * {@link EntityMixinAccess#velocitydamage$setDeltaMovementO(Vec3)} is
     * made. I'll have to be careful of that.
     * @return The approximate acceleration vector, in meters per tick per tick, of the passed <code>Entity</code> as
     * measured over one tick.
     */
    public static Vec3 acceleration(Entity entity) {
        // welp. let's hope I'm accessing the new method properly lol
        Vec3 initialDeltaMovement = ((EntityMixinAccess) entity).velocitydamage$getDeltaMovementO().add(0, -RESTING_Y_DELTA, 0);
        Vec3 finalDeltaMovement = velocity(entity);

        return finalDeltaMovement.subtract(initialDeltaMovement);
    }
}
