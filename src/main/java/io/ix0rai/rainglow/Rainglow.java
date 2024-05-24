package io.ix0rai.rainglow;

import com.google.gson.Gson;
import folk.sisby.kaleido.lib.quiltconfig.api.serializers.TomlSerializer;
import folk.sisby.kaleido.lib.quiltconfig.implementor_api.ConfigEnvironment;
import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.data.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Rainglow implements ModInitializer {
    public static final String MOD_ID = "rainglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String FORMAT = "toml";
    private static final ConfigEnvironment ENVIRONMENT = new ConfigEnvironment(FabricLoader.getInstance().getConfigDir(), FORMAT, TomlSerializer.INSTANCE);
    public static final RainglowConfig CONFIG = RainglowConfig.create(ENVIRONMENT, "", MOD_ID, RainglowConfig.class);
    public static final Gson GSON = new Gson();

    private static final List<RainglowColour> COLOURS = new ArrayList<>();

    public static final String CUSTOM_NBT_KEY = "Colour";

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener((RainglowResourceReloader) () -> id("server_mode_data"));

        PayloadTypeRegistry.playS2C().register(RainglowNetworking.ConfigSyncPayload.PACKET_ID, RainglowNetworking.ConfigSyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RainglowNetworking.ModeSyncPayload.PACKET_ID, RainglowNetworking.ModeSyncPayload.PACKET_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // send modes to client
            RainglowNetworking.syncModes(handler.player);

            // send config to client
            RainglowNetworking.syncConfig(handler.player);
        });
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    public static void setMode(RainglowMode mode) {
        if (mode == null) {
            mode = RainglowMode.get("rainbow");
            LOGGER.warn("attempted to load missing mode, resetting to rainbow");
        }

        COLOURS.clear();

        List<RainglowColour> colours = mode.getColours();
        if (colours.isEmpty()) {
            LOGGER.info("No colours were present in the internal collection, adding blue so that the game doesn't crash");
            colours.add(RainglowColour.BLUE);
        }

        colours.forEach(Rainglow::addColour);
        CONFIG.setInitialized();
    }

    private static void addColour(RainglowColour colour) {
        COLOURS.add(colour);

        if (COLOURS.size() >= 100) {
            throw new RuntimeException("Too many colours registered! Only up to 99 are allowed");
        }
    }

    public static String generateRandomColourId(RandomGenerator random) {
        return COLOURS.get(random.nextInt(COLOURS.size())).getId();
    }

    public static boolean colourUnloaded(RainglowEntity entityType, String colour) {
        return !COLOURS.contains(RainglowColour.get(colour)) && !colour.equals(entityType.getDefaultColour().getId());
    }

    public static String translatableTextKey(String key) {
        if (key.split("\\.").length < 2) throw new IllegalArgumentException("key must be in format \"category.key\": " + key);
        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key, Object... args) {
        return Text.translatable(translatableTextKey(key), args);
    }

    public static Text translatableText(String key) {
        return Text.translatable(translatableTextKey(key));
    }

    public static RainglowColour getColour(RainglowEntity entityType, DataTracker tracker, RandomGenerator random) {
        // generate random colour if the squid's colour isn't currently loaded
        String colour = tracker.get(entityType.getTrackedData());
        if (colourUnloaded(entityType, colour)) {
            // Use last generated colour if not null else generate a new colour
            tracker.set(entityType.getTrackedData(), generateRandomColourId(random));
            colour = tracker.get(entityType.getTrackedData());
        }

        return RainglowColour.get(colour);
    }
}
