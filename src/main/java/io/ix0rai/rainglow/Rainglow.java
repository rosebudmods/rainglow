package io.ix0rai.rainglow;

import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.config.RainglowMode;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static final List<String> COLOUR_IDS = new ArrayList<>();
    private static final List<Pair<SquidColour.RGB, SquidColour.RGB>> PASSIVE_PARTICLE_RGBS = new ArrayList<>();
    private static final List<SquidColour.RGB> INK_PARTICLE_RGBS = new ArrayList<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);
        setMode(CONFIG.getMode());
    }

    public static void setMode(RainglowMode mode) {
        TEXTURES.clear();
        COLOUR_IDS.clear();
        PASSIVE_PARTICLE_RGBS.clear();
        INK_PARTICLE_RGBS.clear();
        mode.getColours().forEach(Rainglow::addColour);
    }

    private static void addColour(SquidColour colour) {
        TEXTURES.put(colour.getId(), colour.getTexture());

        if (TEXTURES.size() == 100) {
            throw new RuntimeException("too many glow squid colours registered! only up to 99 are allowed");
        }

        COLOUR_IDS.add(colour.getId());
        PASSIVE_PARTICLE_RGBS.add(new Pair<>(colour.getPassiveParticleRgb(), colour.getAltPassiveParticleRgb()));
        INK_PARTICLE_RGBS.add(colour.getInkRgb());
    }

    public static Identifier getTexture(String colour) {
        return TEXTURES.get(colour);
    }

    public static int getColourIndex(String colour) {
        return COLOUR_IDS.indexOf(colour);
    }

    public static String getColourId(int index) {
        return COLOUR_IDS.get(index);
    }

    public static SquidColour.RGB getInkRgb(int index) {
        return INK_PARTICLE_RGBS.get(index);
    }

    public static SquidColour.RGB getPassiveParticleRGB(int index, RandomGenerator random) {
        Pair<SquidColour.RGB, SquidColour.RGB> rgbs = PASSIVE_PARTICLE_RGBS.get(index);
        return random.nextBoolean() ? rgbs.getLeft() : rgbs.getRight();
    }

    public static int getColourCount() {
        return COLOUR_IDS.size();
    }

    public static Identifier getDefaultTexture() {
        return TEXTURES.get(COLOUR_IDS.get(0));
    }

    public static String getColour(DataTracker tracker, RandomGenerator random) {
        String colour = tracker.get(COLOUR);
        if (!isColourLoaded(colour)) {
            tracker.set(COLOUR, getColourId(random.nextInt(Rainglow.getColourCount())));
            colour = tracker.get(COLOUR);
        }

        return colour;
    }

    public static boolean isColourLoaded(String colour) {
        return COLOUR_IDS.contains(colour);
    }

    public static String translatableTextKey(String key) {
        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key) {
        return Text.translatable(translatableTextKey(key));
    }
}
