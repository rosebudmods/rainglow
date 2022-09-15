package io.ix0rai.rainglow;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rainglow {
    public static final TrackedData<String> COLOUR;
    public static final Map<String, Identifier> TEXTURES = new HashMap<>();
    public static final List<String> COLOUR_IDS = new ArrayList<>();
    public static final List<Pair<RGB, RGB>> COLOUR_RGBS = new ArrayList<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);

        addColour(
                "blue",
                new Identifier("textures/entity/squid/glow_squid.png"),
                new RGB(0.6F, 1.0F, 0.8F),
                new RGB(0.08F, 0.4F, 0.4F)
        );
        addColour(
                "red",
                new Identifier("textures/entity/squid/red.png"),
                new RGB(1.0F, 1.0F, 0.8F),
                new RGB(1.0F, 0.4F, 0.4F)
        );
    }

    private static void addColour(String colour, Identifier texture, RGB rgb, RGB altRgb) {
        TEXTURES.put(colour, texture);
        COLOUR_IDS.add(colour);
        COLOUR_RGBS.add(new Pair<>(rgb, altRgb));
    }

    public static Identifier getTexture(String colour) {
        return TEXTURES.get(colour);
    }
}
