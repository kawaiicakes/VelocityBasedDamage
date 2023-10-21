package io.github.kawaiicakes.velocitydamage;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import static io.github.kawaiicakes.velocitydamage.PositionCapability.Provider.POSITION_CAP;
import static io.github.kawaiicakes.velocitydamage.VelocityDamageConfig.SERVER;
import static io.github.kawaiicakes.velocitydamage.VelocityDamageConfig.SERVER_SPEC;
import static net.minecraftforge.event.TickEvent.Phase.START;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamage
{
    public static final String MOD_ID = "velocitydamage";
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * For some reason entities on the ground still have a negative delta Y change of this value.
     */
    public static final double RESTING_Y_DELTA = 0.0784000015258789;

    public VelocityDamage() {
        MinecraftForge.EVENT_BUS.register(VelocityDamage.class);
        MinecraftForge.EVENT_BUS.register(PositionCapability.class);
        MinecraftForge.EVENT_BUS.register(VelocityDamageConfig.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!event.phase.equals(START)) return;

        player.getCapability(POSITION_CAP)
                .ifPresent(position -> position.tickPosition(player));
    }

    // Lowest priority so other mods have a chance to change the damage prior to this
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity attacker)) return;

        float originalDamage = event.getAmount();
        LOGGER.debug("Attack pre-change: " + originalDamage);

        double approachVelocity = calculateApproachVelocity(attacker, event.getEntity());
        float newDamage = calculateNewDamage((float) approachVelocity, originalDamage);

        event.setAmount(newDamage);
        LOGGER.debug("Attack post-change: " + newDamage);
        LOGGER.debug("Attacker and target were approaching each other at " + approachVelocity + "m/s.");
    }

    /**
     * Used to get the velocity (in blocks/second) of an entity. A special handling case is made for
     * <code>ServerPlayer</code>s as they do not return the delta movement caused due to input from the corresponding
     * player.
     * @param entity the <code>Entity</code> to return a velocity from.
     * @return  the velocity of the entity relative to the world as an R3 vector.
     */
    public static Vec3 entityVelocity(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            PositionCapability position = player.getCapability(POSITION_CAP).orElseThrow(IllegalStateException::new);
            return player.position().subtract(position.oldPosition).scale(20);
        }

        return entity.getDeltaMovement().add(0, RESTING_Y_DELTA, 0).scale(20);
    }

    /**
     * Positive values indicate that the attacker is approaching the target. Negative indicates that the attacker is
     * retreating from the target.
     */
    public static double calculateApproachVelocity(LivingEntity attacker, LivingEntity target) {
        Vec3 attackerVelocity = entityVelocity(attacker);
        Vec3 targetVelocity = entityVelocity(target);

        if (attackerVelocity.length() == 0 && targetVelocity.length() == 0) return 0;
        Vec3 velocityDifference = attackerVelocity.subtract(targetVelocity);
        Vec3 directionToTarget = target.position().subtract(attacker.position()).normalize();

        return directionToTarget.dot(velocityDifference);
    }

    // TODO: configurable max damage, min damage, velocity multiplier, etc.
    public static float calculateNewDamage(float approachVelocity, float originalDamage) {
        if (Float.isInfinite(originalDamage)) return originalDamage;

        float arbitraryVelocity = approachVelocity / SERVER.velocityIncrement.get().floatValue();
        float multiplier = (float) (Math.pow(arbitraryVelocity, SERVER.exponentiationConstant.get().floatValue()) / 2F);
        float percentageBonus = originalDamage * multiplier; //percentage bonus @ 9.9237m/s is 3.126444021619501

        float maxDamage = originalDamage * SERVER.maxDamagePercent.get().floatValue();
        if (originalDamage + percentageBonus > maxDamage && approachVelocity >= 0) return maxDamage;

        float minDamage = originalDamage * SERVER.minDamagePercent.get().floatValue();
        if (originalDamage - percentageBonus <  originalDamage - (originalDamage * minDamage)
                && approachVelocity < 0) return minDamage;

        return approachVelocity < 0
                        ? (originalDamage - percentageBonus)
                        : (originalDamage + percentageBonus);
    }
}