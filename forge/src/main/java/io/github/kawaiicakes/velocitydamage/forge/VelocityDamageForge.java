package io.github.kawaiicakes.velocitydamage.forge;

import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.velocitydamage.VelocityDamage;
import io.github.kawaiicakes.velocitydamage.api.VelocityDamageMath;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamageForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    public VelocityDamageForge() {
        VelocityDamage.init();

        MinecraftForge.EVENT_BUS.register(VelocityDamageForge.class);
    }

    // Testing purposes only
    @SubscribeEvent
    public static void onRavagerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Ravager ravageHer)) return;
        // I hardly know her!

        LOGGER.info("Ravager absolute acceleration (m/t^2): {}", VelocityDamageMath.accelerationAbs(ravageHer));
    }
}