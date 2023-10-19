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
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static net.minecraftforge.event.TickEvent.Phase.START;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamage
{
    public static final String MOD_ID = "velocitydamage";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final double RESTING_Y_DELTA = 0.0784000015258789;
    public static final double VELOCITY_INCREMENT = 3.2;

    private static final Map<ServerPlayer, Vec3> OLD_COORD_MAP = new HashMap<>();
    private static final Map<ServerPlayer, Vec3> CURRENT_COORD_MAP = new HashMap<>();

    public VelocityDamage() {
        MinecraftForge.EVENT_BUS.register(VelocityDamage.class);
    }

    // This uses ServerTickEvent as it does not need to be ticked by every ServerPlayer instance
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(START)) return;
        OLD_COORD_MAP.clear();
        OLD_COORD_MAP.putAll(CURRENT_COORD_MAP);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!event.phase.equals(START)) return;

        CURRENT_COORD_MAP.put(player, player.position());
    }

    // Lowest priority so other mods have a chance to change the damage prior to this
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        float originalDamage = event.getAmount();
        LOGGER.debug("Attack pre-change: " + originalDamage);

        final float newDamage = calculateNewDamage(returnVelocity(attacker), returnVelocity(event.getEntity()), originalDamage);

        event.setAmount(newDamage);
        LOGGER.debug("Attack post-change: " + newDamage);
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
            return player.position().subtract(OLD_COORD_MAP.get(player)).scale(20);
        }

        return entity.getDeltaMovement().add(0, RESTING_Y_DELTA, 0).scale(20);
    }

    // TODO: configurable max damage, min damage, velocity multiplier, etc.
    private static float calculateNewDamage(Vec3 attackerVelocity, Vec3 targetVelocity, float originalDamage) {
        // Whereas positive denotes the direction approaching the target
        double approachVelocity = attackerVelocity.dot(targetVelocity) != 0
                ? -attackerVelocity.dot(targetVelocity)
                : attackerVelocity.length();

        return originalDamage + ((float) (approachVelocity / VELOCITY_INCREMENT));
    }
}