package io.ix0rai.rainglow;

import com.google.gson.Gson;
import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.networking.RainglowNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rainglow implements ModInitializer {
    public static final String MOD_ID = "rainglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RainglowConfig CONFIG = new RainglowConfig();
    public static final Gson GSON = new Gson();

    public static final TrackedData<String> COLOUR;

    private static final List<SquidColour> COLOURS = new ArrayList<>();
    // we maintain a hash map of textures as well to speed up lookup as much as possible
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener((RainglowResourceReloader) () -> id("server_mode_data"));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (CONFIG.isServerSyncEnabled()) {
                // send modes to client
                RainglowNetworking.sendModeData(handler.player);

                // send config to client
                RainglowNetworking.syncConfig(handler.player);
            }
        });
    }

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    public static void setMode(RainglowMode mode) {
        TEXTURES.clear();
        COLOURS.clear();

        List<SquidColour> colours = mode.isCustom() ? CONFIG.getCustom() : mode.getColours();
        if (colours.isEmpty()) {
            Rainglow.LOGGER.info("no colours were present in the internal collection, adding blue so that the game doesn't crash");
            colours.add(SquidColour.BLUE);
        }
        colours.forEach(Rainglow::addColour);
    }

    public static void refreshColours() {
        // we only ever need to refresh the colours of custom mode, all other sets of colours are immutable
        if (CONFIG.getMode().isCustom()) {
            setMode(RainglowMode.byId("custom"));
        }
    }

    private static void addColour(SquidColour colour) {
        COLOURS.add(colour);
        TEXTURES.put(colour.getId(), colour.getTexture());

        if (COLOURS.size() >= 100) {
            throw new RuntimeException("too many glow squid colours registered! only up to 99 are allowed");
        }
    }

    public static Identifier getTexture(String colour) {
        return TEXTURES.get(colour);
    }

    public static int getColourIndex(String colour) {
        return COLOURS.indexOf(SquidColour.get(colour));
    }

    public static SquidColour.RGB getInkRgb(int index) {
        return COLOURS.get(index).getInkRgb();
    }

    public static SquidColour.RGB getPassiveParticleRGB(int index, RandomGenerator random) {
        SquidColour colour = COLOURS.get(index);
        return random.nextBoolean() ? colour.getPassiveParticleRgb() : colour.getAltPassiveParticleRgb();
    }

    public static SquidColour generateRandomColour(RandomGenerator random) {
        return COLOURS.get(random.nextInt(COLOURS.size()));
    }

    public static Identifier getDefaultTexture() {
        return SquidColour.BLUE.getTexture();
    }

    public static String getColour(DataTracker tracker, RandomGenerator random) {
        // generate random colour if the squid's colour isn't currently loaded
        String colour = tracker.get(COLOUR);
        if (!isColourLoaded(colour)) {
            tracker.set(COLOUR, generateRandomColour(random).getId());
            colour = tracker.get(COLOUR);
        }

        return colour;
    }

    public static boolean isColourLoaded(String colour) {
        return COLOURS.contains(SquidColour.get(colour));
    }

    public static String translatableTextKey(String key) {
        if (key.split("\\.").length != 2) {
            throw new IllegalArgumentException("key must be in format \"category.key\"");
        }

        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key, Object... args) {
        return Text.translatable(translatableTextKey(key), args);
    }

    public static Text translatableText(String key) {
        return Text.translatable(translatableTextKey(key));
    }
}
