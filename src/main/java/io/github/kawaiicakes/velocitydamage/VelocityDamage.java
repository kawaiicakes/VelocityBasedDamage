package io.github.kawaiicakes.velocitydamage;

import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Objects;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamage
{
    public static final String MOD_ID = "velocitydamage";
    private static final Logger LOGGER = LogUtils.getLogger();

    public VelocityDamage()
    {
        MinecraftForge.EVENT_BUS.register(VelocityDamage.class);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LOGGER.debug("Attack pre-change: " + event.getAmount());
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.getMainHandItem().getItem() instanceof SwordItem)) return;
        // 12 seconds
        event.setAmount(event.getAmount() * 400_000F);
        LOGGER.debug("Attack post-change: " + event.getAmount() * 400_000F);
    }
}
