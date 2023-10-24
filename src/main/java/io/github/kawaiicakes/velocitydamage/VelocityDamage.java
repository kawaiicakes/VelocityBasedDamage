package io.github.kawaiicakes.velocitydamage;

import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static io.github.kawaiicakes.velocitydamage.PositionCapability.Provider.POSITION_CAP;
import static io.github.kawaiicakes.velocitydamage.VelocityDamageConfig.SERVER;
import static io.github.kawaiicakes.velocitydamage.VelocityDamageConfig.SERVER_SPEC;
import static net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT;
import static net.minecraftforge.event.TickEvent.Phase.START;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamage
{
    public static final String MOD_ID = "velocitydamage";
    /**
     * For some reason entities on the ground still have a negative delta Y change of this value.
     */
    public static final double RESTING_Y_DELTA = 0.0784000015258789;

    public VelocityDamage() {
        MinecraftForge.EVENT_BUS.register(VelocityDamage.class);
        MinecraftForge.EVENT_BUS.register(PositionCapability.class);
        MinecraftForge.EVENT_BUS.register(VelocityDamageConfig.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(VelocityDamage::onCommonSetup);
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        VelocityPackets.register();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (SERVER.velocityThreshold.get() == 0) return;

        LivingEntity entity = event.getEntity();
        if (entity.isDeadOrDying()) return;
        if (entity.level.isClientSide()) return;
        if (entity instanceof Bat || entity instanceof Player) return;

        Vec3 velocity = entityVelocity(entity).scale((double) 1 / 20);
        if (velocity.scale(20).horizontalDistance() <= SERVER.velocityThreshold.get()) return;

        //noinspection SuspiciousNameCombination
        if (Mth.equal(velocity.x, entity.collide(velocity).x) && Mth.equal(velocity.z, entity.collide(velocity).z)) return;

        LogUtils.getLogger().info("Entity " + entity + " collided at " + velocity.scale(20).horizontalDistance() + " m/s");

        entity.playSound(PLAYER_ATTACK_CRIT, 3.2F, 0.7F);
        // TODO: configurability?
        entity.hurt(DamageSource.FLY_INTO_WALL, (float) ((float) velocity.scale(20).horizontalDistance() - SERVER.velocityThreshold.get()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.phase.equals(START)) return;

        if (event.player instanceof LocalPlayer player) {
            double yDelta = player.onGround ? RESTING_Y_DELTA : 0;
            VelocityPackets.sendToServer(new VelocityPackets.C2SVelocityPacket(player.getDeltaMovement().add(0, yDelta, 0)));
            return;
        }

        if (event.player instanceof ServerPlayer player) {
            player.getCapability(POSITION_CAP)
                    .ifPresent(position -> position.tickPosition(player));
        }
    }

    // Lowest priority so other mods have a chance to change the damage prior to this
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (event.getSource().getDirectEntity() == null) return;
        if (event.getSource().getDirectEntity().level.isClientSide || event.getEntity().level.isClientSide) return;
        if (event.getSource().getDirectEntity() instanceof Projectile && SERVER.projectileMultiplier.get() == 0) return;
        if (event.getSource().getDirectEntity() instanceof AbstractArrow && !(SERVER.wildMode.get())) return;

        Entity attacker = event.getSource().getDirectEntity();

        float originalDamage = event.getAmount();

        double approachVelocity = calculateApproachVelocity(attacker, event.getEntity());
        float newDamage = calculateNewDamage((float) approachVelocity, originalDamage);

        event.setAmount(newDamage);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(SERVER.projectilesHaveMomentum.get())) return;
        if (!(event.getEntity() instanceof Projectile projectile) || projectile.level.isClientSide) return;
        if (projectile.getOwner() == null) return;

        Vec3 ownerVelocity = entityVelocity(projectile.getOwner()).scale((double) 1 / 20);
        if (ownerVelocity.equals(Vec3.ZERO)) return;

        projectile.setDeltaMovement(projectile.getDeltaMovement().add(ownerVelocity));
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
            return position.currentPosition.subtract(position.oldPosition).scale(20);
        }

        return entity.getDeltaMovement().add(0, RESTING_Y_DELTA, 0).scale(20);
    }

    /**
     * Positive values indicate that the attacker is approaching the target. Negative indicates that the attacker is
     * retreating from the target.
     * <br><br>
     * Faithful to true calculations, however; it should be noted that since position is measured at the feet, if the
     * attacker hits the target as it moves upwards relative to the attacker, a debuff is incurred. To fairly rectify
     * this, the eye positions of the entities are also considered.
     */
    public static double calculateApproachVelocity(Entity attacker, LivingEntity target) {
        Vec3 attackerVelocity =
                attacker instanceof Projectile projectile
                ? entityVelocity(projectile).scale(SERVER.projectileMultiplier.get())
                : entityVelocity(attacker);
        Vec3 targetVelocity = entityVelocity(target);

        if (attackerVelocity.length() == 0 && targetVelocity.length() == 0) return 0;

        Vec3 attackerPosition = attacker.position();
        Vec3 targetPosition = target.position();

        // TODO: handle cases where a very small entity attacks a very large one?
        if (targetVelocity.y() - attackerVelocity.y() >= 0 && target.position().y() > attacker.position().y()) attackerPosition = attacker.getEyePosition();
        if (targetVelocity.y() - attackerVelocity.y() <= 0 && target.position().y() < attacker.position().y()) targetPosition = target.getEyePosition();

        Vec3 velocityDifference = attackerVelocity.subtract(targetVelocity);
        Vec3 directionToTarget = targetPosition.subtract(attackerPosition).normalize();

        return directionToTarget.dot(velocityDifference);
    }
    public static float calculateNewDamage(float approachVelocity, float originalDamage) {
        if (approachVelocity == 0) return originalDamage;

        float arbitraryVelocity = Math.abs(approachVelocity) / SERVER.velocityIncrement.get().floatValue();
        float multiplier = (float) (Math.pow(arbitraryVelocity, SERVER.exponentiationConstant.get().floatValue()) / 2F);
        float percentageBonus = originalDamage * multiplier;

        if (approachVelocity < 0) {
            float minDamage = originalDamage * SERVER.minDamagePercent.get().floatValue();
            return Math.max(minDamage, originalDamage - percentageBonus);
        }

        float maxDamage = originalDamage * SERVER.maxDamagePercent.get().floatValue();
        return Math.min(maxDamage, originalDamage + percentageBonus);
    }
}