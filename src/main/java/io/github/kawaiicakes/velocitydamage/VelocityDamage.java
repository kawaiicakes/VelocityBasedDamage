package io.github.kawaiicakes.velocitydamage;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.FishingRodItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamage
{
    public static final String MOD_ID = "velocitydamage";
    private static final Logger LOGGER = LogUtils.getLogger();

    public VelocityDamage()
    {
        MinecraftForge.EVENT_BUS.register(VelocityDamage.class);
    }

    // Lowest priority so other mods have a chance to change the damage prior to this
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LOGGER.debug("Attack pre-change: " + event.getAmount());
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.getMainHandItem().getItem() instanceof FishingRodItem)) return;

        double dotProduct = player.getDeltaMovement().dot(event.getEntity().getDeltaMovement());

        // TODO: find more elegant way to calculate damage
        event.setAmount((float) (event.getAmount() + (1 * -dotProduct)));
        LOGGER.debug("Attack post-change: " + ((float) (event.getAmount() + (1 * -dotProduct))));
    }
}
