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
    public static final double RESTING_Y_DELTA = 0.0784000015258789;
    public static final double VELOCITY_INCREMENT = 3.2;

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
            PositionCapability position = player.getCapability(POSITION_CAP).orElseThrow(IllegalStateException::new);
            return player.position().subtract(position.oldPosition).scale(20);
        }

        return entity.getDeltaMovement().add(0, RESTING_Y_DELTA, 0).scale(20);
    }

    // TODO: configurable max damage, min damage, velocity multiplier, etc.
    // TODO: redo the math here lol
    private static float calculateNewDamage(Vec3 attackerVelocity, Vec3 targetVelocity, float originalDamage) {
        // Whereas positive denotes the direction approaching the target
        double approachVelocity = attackerVelocity.dot(targetVelocity) != 0
                ? -attackerVelocity.dot(targetVelocity)
                : attackerVelocity.length();

        return originalDamage + ((float) (approachVelocity / VELOCITY_INCREMENT));
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