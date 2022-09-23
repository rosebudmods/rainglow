package io.ix0rai.rainglow;

import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.config.RainglowMode;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        mode.getColours().forEach(Rainglow::addColour);
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
        return TEXTURES.get(COLOURS.get(0).getId());
    }

    public static String getColour(DataTracker tracker, RandomGenerator random) {
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
        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key) {
        return Text.translatable(translatableTextKey(key));
    }
}
