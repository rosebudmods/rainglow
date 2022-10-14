package io.ix0rai.rainglow;

import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.config.RainglowMode;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
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

public class Rainglow {
    public static final String MOD_ID = "rainglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RainglowConfig CONFIG = new RainglowConfig();

    public static final TrackedData<String> COLOUR;

    private static final List<SquidColour> COLOURS = new ArrayList<>();
    // we maintain a hash map of textures as well to speed up lookup as much as possible
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);
        setMode(CONFIG.getMode());
    }

    public static void setMode(RainglowMode mode) {
        TEXTURES.clear();
        COLOURS.clear();

        List<SquidColour> colours = mode == RainglowMode.CUSTOM ? CONFIG.getCustom() : mode.getColours();
        if (colours.isEmpty()) {
            Rainglow.LOGGER.info("no colours were added to the list, adding blue so that the game doesn't crash");
            colours.add(SquidColour.BLUE);
        }
        colours.forEach(Rainglow::addColour);
    }

    public static void refreshColours() {
        // we only ever need to refresh the colours of custom mode, all other sets of colours are immutable
        if (CONFIG.getMode() == RainglowMode.CUSTOM) {
            setMode(RainglowMode.CUSTOM);
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

    public static SquidColour generateRandomColour(Random random) {
        return COLOURS.get(random.nextInt(COLOURS.size()));
    }

    public static Identifier getDefaultTexture() {
        return SquidColour.BLUE.getTexture();
    }

    public static String getColour(DataTracker tracker, Random random) {
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
        return new TranslatableText(translatableTextKey(key), args);
    }

    public static Text translatableText(String key) {
        return new TranslatableText(translatableTextKey(key));
    }
}
