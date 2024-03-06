package io.github.kawaiicakes.velocitydamage.api;

import io.github.kawaiicakes.velocitydamage.config.ConfigValues;

public class DamageModule {
    /**
     * Call and use this in the correct place (dependent on loader). Pretty much all the finer functions have been
     * abstracted, including application of config values. Use in conjunction with {@link VelocityDamageMath} for
     * maximum ease.
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

    // TODO: damage from acceleration calculations
    // TODO: momentum inheritance module (momentum inheritance for projectiles, angle of bounce when colliding with walls)

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
