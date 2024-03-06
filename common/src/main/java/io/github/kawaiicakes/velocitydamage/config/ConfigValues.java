package io.github.kawaiicakes.velocitydamage.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.ApiStatus;

/* TODO: this not a final list of config values. More should be determined, and some should be assigned dependent on
    other values. (e.g. damageOnAcceleration is not explicitly defined in config implementations, but is true if the
    acceleration threshold is greater than 0)
 */
public class ConfigValues {
    public static boolean DEFAULT_SPEED_DAMAGE_ENABLED = true;
    public static boolean DEFAULT_MOMENTUM_INHERITANCE_ENABLED = true;
    public static boolean DEFAULT_WILD_MODE = false;
    public static float DEFAULT_VELOCITY_INCREMENT = 6.90F;
    public static float DEFAULT_EXPONENTIATION = 1.20F;
    public static final float DEFAULT_MINIMUM_DMG = 0.40F;
    public static final float DEFAULT_MAXIMUM_DMG = Float.MAX_VALUE;
    public static final float DEFAULT_PROJECTILE_MULTIPLIER = 1.00F;
    // TODO: determine a proper value
    public static final float DEFAULT_ACCELERATION_THRESHOLD = 9.81F;

    @ApiStatus.Internal
    public static ConfigValues CONFIG;

    public final boolean speedDamageBonus, momentumInheritance, damageOnAcceleration, wildMode;
    public final float velocityIncrement, exponentiationConstant, minDamagePercent,
            maxDamagePercent, projectileMultiplier, accelerationThreshold;

    @ApiStatus.Internal
    public ConfigValues(
            boolean speedDamageBonus,
            boolean momentumInheritance,
            boolean damageOnAcceleration,
            boolean wildMode,
            float velocityIncrement,
            float exponentiationConstant,
            float minDamagePercent,
            float maxDamagePercent,
            float projectileMultiplier,
            float accelerationThreshold
    ) {
        this.speedDamageBonus = speedDamageBonus;
        this.momentumInheritance = momentumInheritance;
        this.damageOnAcceleration = damageOnAcceleration;
        this.wildMode = wildMode;
        this.velocityIncrement = velocityIncrement;
        this.exponentiationConstant = exponentiationConstant;
        this.minDamagePercent = minDamagePercent;
        this.maxDamagePercent = maxDamagePercent;
        this.projectileMultiplier = projectileMultiplier;
        this.accelerationThreshold = accelerationThreshold;
    }

    /**
     * This method is "abstracted" and should be implemented on a per modloader basis. Its goal is to instantiate
     * <code>ConfigValues</code> with the values taken from the config in the implementing modloader, then to
     * assign that instance to the <code>CONFIG</code> field to make this a singleton pattern. This should ideally
     * be called when serverside data is being reloaded to allow mod behaviour to be changed without a full server
     * restart.
     */
    @ExpectPlatform
    private static ConfigValues generateConfig() {
        throw new AssertionError("Whoopsie doopsies! Implementation for this modloader not found!");
    }

    /**
     * This should ideally be called when serverside data is being reloaded to allow mod behaviour to be changed
     * without a full server restart.
     */
    public static ConfigValues getInstance() {
        return CONFIG == null ? generateConfig() : CONFIG;
    }
}
