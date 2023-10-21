package io.github.kawaiicakes.velocitydamage;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class VelocityDamageConfig {
    public static final float DEFAULT_SQUASH = 6.90F;
    public static final float DEFAULT_EXPONENTIATION = 3.20F;
    // FIXME: An iron golem continually attacking me will sometimes manage to be moving away; often enough to get down to the previous minimum of 10% damage.
    public static final float DEFAULT_MINIMUM_DMG = 0.40F;
    public static final float DEFAULT_MAXIMUM_DMG = Float.MAX_VALUE;

    protected static ForgeConfigSpec SERVER_SPEC;
    protected static ConfigValues SERVER;

    static {
        Pair<ConfigValues, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ConfigValues::new);
        SERVER_SPEC = pair.getRight();
        SERVER = pair.getLeft();
    }

    public static class ConfigValues {
        /**
         * Arbitrary value. The function f(x) represents the % increase of the original damage and is equal to
         * ((x / VELOCITY_INCREMENT)^2) / 2; where x indicates one-dimensional velocity in the direction of the target
         * (when positive). In other words, x is the speed with which the attacker is approaching (or for that matter,
         * retreating from) the target.
         * <br><br>
         * The player by default sprints at 5.612m/s. When VELOCITY_INCREMENT is the default 3.96828326, a player sprinting
         * into a stationary target will have a 100% bonus on their attack. The fastest horses in vanilla Minecraft
         * have a top speed of 14.23m/s. Using the formula at the default VELOCITY_INCREMENT, this returns as a
         * 643% percent increase in damage.
         */
        public final ForgeConfigSpec.DoubleValue velocityIncrement;
        public final ForgeConfigSpec.DoubleValue exponentiationConstant;
        /**
         * The minimum damage dealt is capped to this percentage of the original. Must be a value from 0.0 to 1.0 inclusive.
         * The minimum is capped at 10% by default.
         */
        public final ForgeConfigSpec.DoubleValue minDamagePercent;
        /**
         * The maximum bonus damage one can inflict is capped to this percentage of the original. Must be greater than 0.
         * There is no maximum by default.
         */
        public final ForgeConfigSpec.DoubleValue maxDamagePercent;

        protected ConfigValues(ForgeConfigSpec.Builder builder) {
            builder.push("Velocity-Based Damage Config");
            this.velocityIncrement = builder
                    .comment("\"Increases\" the necessary velocity to do an arbitrary damage by a factor of this.")
                    .translation(key("velocityIncrement"))
                    .defineInRange("velocityIncrement", DEFAULT_SQUASH, 1, Float.MAX_VALUE);

            this.exponentiationConstant = builder
                    .comment("Changes the power of the damage calculation function. Determines growth curve.")
                    .translation(key("exponentiationConstant"))
                    .defineInRange("exponentiationConstant", DEFAULT_EXPONENTIATION, 0, Float.MAX_VALUE);

            this.minDamagePercent = builder
                    .comment("The minimum amount of damage, as a percentage of the original, that a debuffed attack may do.")
                    .translation(key("minDamagePercent"))
                    .defineInRange("minDamagePercent", DEFAULT_MINIMUM_DMG, 0, 1.0);

            this.maxDamagePercent = builder
                    .comment("The maximum bonus amount of damage, as a percentage of the original, that a buffed attack may do.")
                    .translation(key("maxDamagePercent"))
                    .defineInRange("maxDamagePercent", DEFAULT_MAXIMUM_DMG, 0, Float.MAX_VALUE);

            builder.pop();
        }

        private static String key(String valueName) {
            return "config.velocitydamage." + valueName;
        }
    }
}
