package io.github.kawaiicakes.velocitydamage.config.forge;

import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.velocitydamage.config.ConfigValues;
import org.slf4j.Logger;

import static io.github.kawaiicakes.velocitydamage.config.ConfigValues.*;
import static io.github.kawaiicakes.velocitydamage.config.forge.ForgeConfig.SERVER;

public class ConfigValuesImpl {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ConfigValues generateConfig() {
        try {
            ConfigValues.CONFIG = new ConfigValues(
                    SERVER.speedDamageBonus.get(),
                    SERVER.projectilesHaveMomentum.get(),
                    SERVER.accelerationThreshold.get() > 0,
                    SERVER.wildMode.get(),
                    SERVER.velocityIncrement.get().floatValue(),
                    SERVER.exponentiationConstant.get().floatValue(),
                    SERVER.minDamagePercent.get().floatValue(),
                    SERVER.maxDamagePercent.get().floatValue(),
                    SERVER.projectileMultiplier.get().floatValue(),
                    SERVER.accelerationThreshold.get().floatValue()
            );

            return ConfigValues.CONFIG;
        } catch (Throwable e) {
            LOGGER.error("Unable to load settings from config!", e);
            LOGGER.error("Falling back on default values!");

            ConfigValues.CONFIG = new ConfigValues(
                    DEFAULT_SPEED_DAMAGE_ENABLED,
                    DEFAULT_MOMENTUM_INHERITANCE_ENABLED,
                    true,
                    DEFAULT_WILD_MODE,
                    DEFAULT_VELOCITY_INCREMENT,
                    DEFAULT_EXPONENTIATION,
                    DEFAULT_MINIMUM_DMG,
                    DEFAULT_MAXIMUM_DMG,
                    DEFAULT_PROJECTILE_MULTIPLIER,
                    DEFAULT_ACCELERATION_THRESHOLD
            );
            return CONFIG;
        }
    }
}
