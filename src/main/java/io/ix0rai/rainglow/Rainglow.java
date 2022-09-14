package io.ix0rai.rainglow;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rainglow {
    public static final TrackedData<String> COLOUR;
    public static final Map<String, Identifier> TEXTURES = new HashMap<>();
    public static final List<String> COLOUR_IDS = new ArrayList<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);

        addTexture("blue", new Identifier("textures/entity/squid/glow_squid.png"));
        addTexture("red", new Identifier("textures/entity/squid/red.png"));
    }

    private static void addTexture(String colour, Identifier texture) {
        TEXTURES.put(colour, texture);
        COLOUR_IDS.add(colour);
    }

    public static Identifier getTexture(String colour) {
        return TEXTURES.get(colour);
    }
}
