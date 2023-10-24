package io.github.kawaiicakes.velocitydamage;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static io.github.kawaiicakes.velocitydamage.VelocityDamage.MOD_ID;
import static io.github.kawaiicakes.velocitydamage.VelocityDamageConfig.SERVER;
import static net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_CRIT;

public class VelocityPackets {
    private static final String PROTOCOL_VERSION = "1.0";
    private static SimpleChannel INSTANCE;
    private static int PACKET_ID = 1;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static SimpleChannel getInstance() {
        return INSTANCE;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MOD_ID, "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
                .serverAcceptedVersions(s -> s.equals(PROTOCOL_VERSION))
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(C2SVelocityPacket.class, PACKET_ID++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(C2SVelocityPacket::encode)
                .decoder(C2SVelocityPacket::new)
                .consumerMainThread(C2SVelocityPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

    public static class C2SVelocityPacket {
        private final Vec3 velocity;

        public C2SVelocityPacket(Vec3 velocity) {
            this.velocity = velocity;
        }

        public C2SVelocityPacket(FriendlyByteBuf buf) {
            this.velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeDouble(this.velocity.x);
            buf.writeDouble(this.velocity.y);
            buf.writeDouble(this.velocity.z);
        }

        public void handle(Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() == null) return;

                Vec3 naiveVelocity = VelocityDamage.entityVelocity(context.getSender()).scale((double) 1/20);
                Vec3 preferredValue =
                        Math.min(this.velocity.horizontalDistance(), naiveVelocity.horizontalDistance()) == this.velocity.horizontalDistance()
                                ? this.velocity : naiveVelocity;

                LOGGER.info(preferredValue.scale(20).horizontalDistance() + " m/s");

                if (preferredValue.scale(20).horizontalDistance() <= SERVER.velocityThreshold.get() + 1) return;

                //noinspection SuspiciousNameCombination
                if (Mth.equal(preferredValue.x, context.getSender().collide(preferredValue).x) && Mth.equal(preferredValue.z, context.getSender().collide(preferredValue).z)) return;

                LogUtils.getLogger().info("Entity " + context.getSender() + " collided at " + velocity.scale(20).horizontalDistance() + " m/s");

                context.getSender().playSound(PLAYER_ATTACK_CRIT, 3.2F, 0.7F);
                // TODO: configurability?
                context.getSender().hurt(DamageSource.FLY_INTO_WALL, (float) ((float) preferredValue.scale(20).horizontalDistance() - SERVER.velocityThreshold.get()));
            });
        }
    }
}
