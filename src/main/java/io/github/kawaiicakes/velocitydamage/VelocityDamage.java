package io.github.kawaiicakes.velocitydamage;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static io.github.kawaiicakes.velocitydamage.VelocityDamage.PositionCapabilityProvider.POSITION_CAP;
import static net.minecraftforge.event.TickEvent.Phase.START;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamage
{
    public static final String MOD_ID = "velocitydamage";
    public static final ResourceLocation POSITION_CAPABILITY_ID = new ResourceLocation(MOD_ID, "delta_position");
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * For some reason entities on the ground still have a negative delta Y change of this value.
     */
    public static final double RESTING_Y_DELTA = 0.0784000015258789;
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
    public static double VELOCITY_INCREMENT = 3.96828326;
    /**
     * The minimum damage dealt is capped to this percentage of the original. Must be a value from 0.0 to 1.0 inclusive.
     * The minimum is capped at 10% by default.
     */
    public static float MINIMUM_DAMAGE_PERCENTAGE = 0.10F;
    /**
     * The maximum bonus damage one can inflict is capped to this percentage of the original. Must be greater than 0.
     * There is no maximum by default.
     */
    public static float MAXIMUM_DAMAGE_PERCENTAGE = Float.MAX_VALUE;

    public VelocityDamage() {
        MinecraftForge.EVENT_BUS.register(VelocityDamage.class);
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
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        float originalDamage = event.getAmount();
        LOGGER.debug("Attack pre-change: " + originalDamage);

        double approachVelocity = calculateApproachVelocity(attacker, event.getEntity());
        float newDamage = calculateNewDamage(approachVelocity, originalDamage);

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
    private static Vec3 returnVelocity(Entity entity) {
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
    private static double calculateApproachVelocity(LivingEntity attacker, LivingEntity target) {
        Vec3 attackerVelocity = returnVelocity(attacker);
        Vec3 targetVelocity = returnVelocity(target);

        if (attackerVelocity.length() == 0 && targetVelocity.length() == 0) return 0;

        double approachSpeed = targetVelocity.subtract(attackerVelocity).length();

        Vec3 directionToTarget = target.position().subtract(attacker.position()).normalize();
        double attackerToTargetVelocityComponent = directionToTarget.dot(attackerVelocity);

        if (directionToTarget.length() == 0) return approachSpeed;
        if (attackerToTargetVelocityComponent < 0) return -approachSpeed;
        if (attackerToTargetVelocityComponent > 0) return approachSpeed;

        return directionToTarget.reverse().dot(targetVelocity);
    }

    // TODO: configurable max damage, min damage, velocity multiplier, etc.
    private static float calculateNewDamage(double approachVelocity, float originalDamage) {
        if (Float.isInfinite(originalDamage)) return originalDamage;

        double arbitraryVelocity = approachVelocity / VELOCITY_INCREMENT;
        double multiplier = (arbitraryVelocity * arbitraryVelocity) / 2;
        double percentageBonus = originalDamage * multiplier; //percentage bonus @ 9.9237... m/s is 3.126444021619501

        float maxDamage = (Float.isInfinite(originalDamage * MAXIMUM_DAMAGE_PERCENTAGE))
                ? Float.MAX_VALUE
                : originalDamage * MAXIMUM_DAMAGE_PERCENTAGE;

        if (percentageBonus >= Float.POSITIVE_INFINITY) return maxDamage;
        if (originalDamage + percentageBonus >= maxDamage) return maxDamage;

        float minDamage = originalDamage * MINIMUM_DAMAGE_PERCENTAGE;

        if (percentageBonus <= Float.NEGATIVE_INFINITY) return minDamage;
        if (originalDamage - percentageBonus <= minDamage) return minDamage;

        return (float) (approachVelocity < 0
                        ? (originalDamage - percentageBonus)
                        : (originalDamage + percentageBonus));
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesEvent(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof ServerPlayer player)) return;
        if ((player.getCapability(POSITION_CAP).isPresent())) return;

        event.addCapability(POSITION_CAPABILITY_ID, new PositionCapabilityProvider());
    }

    protected static class PositionCapabilityProvider implements ICapabilityProvider {
        public static Capability<PositionCapability> POSITION_CAP = CapabilityManager.get(new CapabilityToken<>() {});

        private PositionCapability capability = null;
        private final LazyOptional<PositionCapability> lazyHandler = LazyOptional.of(this::createCapability);

        private PositionCapability createCapability() {
            if (this.capability == null) {
                this.capability = new PositionCapability();
            }
            return this.capability;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == POSITION_CAP) {
                return lazyHandler.cast();
            }

            return LazyOptional.empty();
        }
    }

    protected static class PositionCapability {
        public Vec3 oldPosition;
        public Vec3 currentPosition;

        public void tickPosition(ServerPlayer player) {
            oldPosition = currentPosition;
            currentPosition = player.position();
        }
    }
}