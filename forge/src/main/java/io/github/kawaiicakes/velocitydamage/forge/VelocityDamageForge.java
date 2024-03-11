package io.github.kawaiicakes.velocitydamage.forge;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.velocitydamage.VelocityDamage;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.data.DataProvider.KEY_COMPARATOR;

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
    @SubscribeEvent
    public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getSide().equals(LogicalSide.CLIENT)) return;

        if (STOP_LOGGING) return;

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

        JsonObject jsonData = new JsonObject();
        jsonData.add("xO", xO);
        jsonData.add("yO", yO);
        jsonData.add("zO", zO);
        jsonData.add("xOld", xOld);
        jsonData.add("yOld", yOld);
        jsonData.add("zOld", zOld);
        jsonData.add("pos", pos);
        jsonData.add("deltaPos", deltaPos);
        jsonData.add("deltaPosAbs", deltaPosAbs);

        Path jsonFileParentPath = FMLPaths.GAMEDIR.relative().resolve("logs").resolve("velocitydamage");

        String jsonFileName = "kinematics_data.json";

        Path jsonFilePath;

        try {
            jsonFilePath = FileUtil.createPathToResource(jsonFileParentPath, "kinematics_data", ".json");

            if (!jsonFilePath.startsWith(jsonFileParentPath) || !FileUtil.isPathNormalized(jsonFilePath) || !FileUtil.isPathPortable(jsonFilePath))
                throw new ResourceLocationException("Invalid resource path: " + jsonFilePath);
        } catch (InvalidPathException invalidpathexception) {
            LOGGER.error("Invalid resource path: {}", jsonFileName);
            return;
        }

        try {
            Files.createDirectories(Files.exists(jsonFileParentPath) ? jsonFileParentPath.toRealPath() : jsonFileParentPath);
        } catch (IOException ioexception) {
            LOGGER.error("Failed to create parent directory: {}", jsonFileParentPath);
            return;
        }

        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            // ignoring deprecation here because SHA-1 interoperability is desired (probably? lol)
            //noinspection UnstableApiUsage,deprecation
            HashingOutputStream hashOutput = new HashingOutputStream(Hashing.sha1(), byteOutput);

            Writer writer = new OutputStreamWriter(hashOutput, StandardCharsets.UTF_8);
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setSerializeNulls(false);
            jsonWriter.setIndent("  ");
            GsonHelper.writeValue(jsonWriter, jsonData, KEY_COMPARATOR);
            jsonWriter.close();
            try {
                Files.write(jsonFilePath, byteOutput.toByteArray());
            } catch (Throwable e) {
                try {
                    byteOutput.close();
                } catch (Throwable throwable) {
                    e.addSuppressed(throwable);
                }

                throw e;
            }

            byteOutput.close();
        } catch (Throwable e2) {
            LOGGER.error("Error while saving!", e2);
        }
    }

    // DEBUG ONLY
    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {
        public static final IGuiOverlay DEBUGGER = (((forgeGui, arg, f, i, j) -> {
            GuiComponent.drawCenteredString(arg, Minecraft.getInstance().font, String.valueOf(TICK), i/2, j/2, 0xFFFF00);
        }));

        @SubscribeEvent
        public static void registerGui(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("temp_shit", DEBUGGER);
        }
    }
}