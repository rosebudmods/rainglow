package io.ix0rai.rainglow;

import com.google.gson.Gson;
import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.data.RainglowNetworking;
import io.ix0rai.rainglow.data.RainglowResourceReloader;
import io.ix0rai.rainglow.data.SquidColour;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Rainglow implements ModInitializer {
    public static final String MOD_ID = "rainglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RainglowConfig CONFIG = new RainglowConfig();
    public static final Gson GSON = new Gson();

    private static final List<SquidColour> COLOURS = new ArrayList<>();
    // we maintain a hash map of textures as well to speed up lookup as much as possible
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static TrackedData<String> colour;

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

        List<SquidColour> colours = mode.getColours();
        if (colours.isEmpty()) {
            LOGGER.info("no colours were present in the internal collection, adding blue so that the game doesn't crash");
            colours.add(SquidColour.BLUE);
        }
        colours.forEach(Rainglow::addColour);
    }

    public static void refreshColours() {
        // we only ever need to refresh the colours of custom mode, all other sets of colours are immutable
        if (CONFIG.getMode().getId().equals("custom")) {
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

    public static SquidColour.RGB getPassiveParticleRGB(int index, Random random) {
        SquidColour colour = COLOURS.get(index);
        return random.nextBoolean() ? colour.getPassiveParticleRgb() : colour.getAltPassiveParticleRgb();
    }

    public static String generateRandomColourId(Random random) {
        return COLOURS.get(random.nextInt(COLOURS.size())).getId();
    }

    public static Identifier getDefaultTexture() {
        return SquidColour.BLUE.getTexture();
    }

    public static boolean colourUnloaded(String colour) {
        return !COLOURS.contains(SquidColour.get(colour));
    }

    public static String translatableTextKey(String key) {
        if (key.split("\\.").length != 2) {
            throw new IllegalArgumentException("key must be in format \"category.key\"");
        }

        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key, Object... args) {
        return new TranslatableText(translatableTextKey(key), args);
    }

    public static Text translatableText(String key) {
        return new TranslatableText(translatableTextKey(key));
    }

    public static TrackedData<String> getTrackedColourData() {
        // we cannot statically load the tracked data because then it gets registered too early
        // it breaks the squids' other tracked data, their dark ticks after being hurt
        // this is a workaround to make sure the data is registered at the right time
        // we simply ensure it isn't loaded until it's needed, and that fixes the issue
        if (colour == null) {
            colour = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);
        }

        return colour;
    }

    public static String getColour(DataTracker tracker, Random random) {
        // generate random colour if the squid's colour isn't currently loaded
        String colour = tracker.get(getTrackedColourData());
        if (colourUnloaded(colour)) {
            // Use last generated colour if not null else generate a new colour
            tracker.set(getTrackedColourData(), generateRandomColourId(random));
            colour = tracker.get(getTrackedColourData());
        }

        return colour;
    }
}
