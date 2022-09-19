package io.ix0rai.rainglow;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rainglow {
    public static final TrackedData<String> COLOUR;
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static final List<String> COLOUR_IDS = new ArrayList<>();
    private static final List<Pair<RGB, RGB>> PASSIVE_PARTICLE_RGBS = new ArrayList<>();
    private static final List<RGB> INK_PARTICLE_RGB = new ArrayList<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);

        // blue is the vanilla glow squid colour
        addColour(
                "blue",
                new Identifier("textures/entity/squid/glow_squid.png"),
                new RGB(0.6F, 1.0F, 0.8F),
                new RGB(0.08F, 0.4F, 0.4F),
                new RGB(204, 31, 102)
        );

        // custom colours
        addColour(
                "red",
                new Identifier("textures/entity/squid/red.png"),
                new RGB(1.0F, 1.0F, 0.8F),
                new RGB(1.0F, 0.4F, 0.4F),
                new RGB(200, 0, 0)
        );
        addColour(
                "green",
                new Identifier("textures/entity/squid/green.png"),
                new RGB(0.6F, 1.0F, 0.8F),
                new RGB(0.08F, 1.0F, 0.4F),
                new RGB(0, 200, 0)
        );
    }

    private static void addColour(String colour, Identifier texture, RGB rgb, RGB altRgb, RGB inkRgb) {
        TEXTURES.put(colour, texture);

        if (TEXTURES.size() == 100) {
            throw new RuntimeException("too many glow squid colours registered! only up to 99 are allowed");
        }

        COLOUR_IDS.add(colour);
        PASSIVE_PARTICLE_RGBS.add(new Pair<>(rgb, altRgb));
        INK_PARTICLE_RGB.add(inkRgb);
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

    public static RGB getInkRgb(int index) {
        return INK_PARTICLE_RGB.get(index);
    }

    public static RGB getPassiveParticleRGB(int index, Random random) {
        Pair<RGB, RGB> rgbs = PASSIVE_PARTICLE_RGBS.get(index);
        return random.nextBoolean() ? rgbs.getLeft() : rgbs.getRight();
    }

    public static int getColourCount() {
        return COLOUR_IDS.size();
    }

    public static Identifier getDefaultTexture() {
        return TEXTURES.get("blue");
    }

    public static boolean isColourLoaded(String colour) {
        return COLOUR_IDS.contains(colour);
    }
}
