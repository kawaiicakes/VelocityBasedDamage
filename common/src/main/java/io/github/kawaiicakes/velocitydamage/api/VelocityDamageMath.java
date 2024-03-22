package io.github.kawaiicakes.velocitydamage.api;

import io.github.kawaiicakes.velocitydamage.config.ConfigValues;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Utility-class containing static methods for easily calculating things related to the function of this mod.
 * Config values are automatically taken into account, where applicable.
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
     * @param approachVelocity  the <code>double</code> in meters per tick at which the attacking entity approaches
     *                          (or retreats) from the target.
     * @param originalDamage    the original damage to be conferred to the target.
     * @return a <code>float</code> representing the new damage to apply to the attacked entity.
     */
    public static float damageOnAttack(double approachVelocity, float originalDamage) {
        if (!isEnabled() || approachVelocity == 0.00) return originalDamage;

        float arbitraryVelocity = (float) (Math.abs(approachVelocity) / velocityIncrement());
        float multiplier = (float) (Math.pow(arbitraryVelocity, exponentiationConstant()) / 2F);
        float percentageBonus = originalDamage * multiplier;

        if (approachVelocity < 0.00) {
            float minDamage = originalDamage * minDamagePercent();
            return Math.max(minDamage, originalDamage - percentageBonus);
        }

        float maxDamage = originalDamage * maxDamagePercent();
        return Math.min(maxDamage, originalDamage + percentageBonus);
    }

    /*
        HELPER METHODS BELOW
     */

    private static boolean isEnabled() {
        return ConfigValues.getInstance().speedDamageBonus;
    }

    private static float velocityIncrement() {
        return ConfigValues.getInstance().velocityIncrement;
    }

    private static float exponentiationConstant() {
        return ConfigValues.getInstance().exponentiationConstant;
    }

    private static float minDamagePercent() {
        return ConfigValues.getInstance().minDamagePercent;
    }

    private static float maxDamagePercent() {
        return ConfigValues.getInstance().maxDamagePercent;
    }
}
