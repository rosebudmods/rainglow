package io.ix0rai.rainglow.data;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public enum RainglowEntity {
    GLOW_SQUID("glow_squid"),
    ALLAY("allay"),
    SLIME("slime");

    private static final HashMap<String, RainglowEntity> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private final String id;

    RainglowEntity(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static RainglowEntity read(PacketByteBuf buf) {
        return get(buf.readString());
    }

    public static void write(PacketByteBuf buf, RainglowEntity entity) {
        buf.writeString(entity.id);
    }

    @Nullable
    public static RainglowEntity get(String id) {
        return BY_ID.get(id);
    }
}
