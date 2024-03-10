package io.github.kawaiicakes.velocitydamage.forge;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.velocitydamage.VelocityDamage;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(VelocityDamage.MOD_ID)
public class VelocityDamageForge {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<Double> XO = new ArrayList<>();
    private static final List<Double> YO = new ArrayList<>();
    private static final List<Double> ZO = new ArrayList<>();
    private static final List<Double> X_OLD = new ArrayList<>();
    private static final List<Double> Y_OLD = new ArrayList<>();
    private static final List<Double> Z_OLD = new ArrayList<>();
    private static final List<Vec3> POSITION = new ArrayList<>();
    private static final List<Vec3> DELTA_MOVEMENT = new ArrayList<>();

    private static boolean STOP_LOGGING = false;

    private static int TICK = 0;

    public VelocityDamageForge() {
        VelocityDamage.init();

        MinecraftForge.EVENT_BUS.register(VelocityDamageForge.class);
    }

    // TODO: display tick # on screen (odd for start of tick, even for end of tick)
    // Testing purposes only
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (STOP_LOGGING) return;
        if (!(event.player instanceof LocalPlayer playHer)) return;
        // I hardly know her!

        TICK++;

        XO.add(playHer.xo);
        YO.add(playHer.yo);
        ZO.add(playHer.zo);

        X_OLD.add(playHer.xOld);
        Y_OLD.add(playHer.yOld);
        Z_OLD.add(playHer.zOld);

        POSITION.add(playHer.position());

        DELTA_MOVEMENT.add(playHer.getDeltaMovement());
    }

    // TESTING ONLY
    // TODO: save data to json
    @SubscribeEvent
    public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getSide().equals(LogicalSide.CLIENT)) return;

        STOP_LOGGING = true;

        JsonArray xO = new JsonArray(XO.size());
        for (Double ddddouble : XO) {
            xO.add(ddddouble);
        }
        JsonArray yO = new JsonArray(YO.size());
        for (Double ddddouble : YO) {
            yO.add(ddddouble);
        }
        JsonArray zO = new JsonArray(ZO.size());
        for (Double ddddouble : ZO) {
            zO.add(ddddouble);
        }
        JsonArray xOld = new JsonArray(X_OLD.size());
        for (Double ddddouble : X_OLD) {
            xOld.add(ddddouble);
        }
        JsonArray yOld = new JsonArray(Y_OLD.size());
        for (Double ddddouble : Y_OLD) {
            yOld.add(ddddouble);
        }
        JsonArray zOld = new JsonArray(Z_OLD.size());
        for (Double ddddouble : Z_OLD) {
            zOld.add(ddddouble);
        }
        JsonArray pos = new JsonArray(POSITION.size());
        for (Vec3 position : POSITION) {
            pos.add(position.toString());
        }
        JsonArray deltaPos = new JsonArray(DELTA_MOVEMENT.size());
        JsonArray deltaPosAbs = new JsonArray(DELTA_MOVEMENT.size());
        for (Vec3 vector : DELTA_MOVEMENT) {
            deltaPos.add(vector.toString());
            deltaPosAbs.add(vector.length());
        }
    }
}