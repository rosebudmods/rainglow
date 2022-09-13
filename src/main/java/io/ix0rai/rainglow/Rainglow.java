package io.ix0rai.rainglow;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class Rainglow {
    public static final TrackedData<String> COLOUR;
    public static final Map<String, Identifier> TEXTURES = new HashMap<>();

    static {
        COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);
    }

    static {
        TEXTURES.put("blue", new Identifier("textures/entity/squid/glow_squid.png"));
    }

    public static Identifier getTexture(String colour) {
        return TEXTURES.get(colour);
    }
}
