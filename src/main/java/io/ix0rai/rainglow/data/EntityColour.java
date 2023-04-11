package io.ix0rai.rainglow.data;

import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashMap;

public enum EntityColour {
    BLACK(new RGB(0.0F, 0.0F, 0.0F), new RGB(0.0F, 0.0F, 0.0F), new RGB(0, 0, 0)),
    BLUE(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102)),
    BROWN(new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(149, 59, 35)),
    CYAN(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102)),
    GRAY(new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100)),
    GREEN(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0)),
    INDIGO(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 1.0F), new RGB(0, 0, 200)),
    LIGHT_BLUE(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102)),
    LIGHT_GRAY(new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100)),
    LIME(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0)),
    MAGENTA(new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100)),
    ORANGE(new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0)),
    PINK(new RGB(0.6F, 0F, 0.5F), new RGB(1.0F, 0.1F, 1.0F), new RGB(200, 0, 0)),
    PURPLE(new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100)),
    RED(new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0)),
    WHITE(new RGB(1.0F, 1.0F, 1.0F), new RGB(1.0F, 1.0F, 1.0F), new RGB(200, 200, 200)),
    YELLOW(new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 1.0F, 0.4F), new RGB(200, 0, 0));

    private static final HashMap<String, EntityColour> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private Identifier texture;
    private final RGB passiveParticleRgb;
    private final RGB altPassiveParticleRgb;
    private final RGB inkRgb;

    EntityColour(RGB passiveParticleRgb, RGB altPassiveParticleRgb, RGB inkRgb) {
        this.texture = new Identifier("textures/entity/squid/" + this.getId() + ".png");
        this.passiveParticleRgb = passiveParticleRgb;
        this.altPassiveParticleRgb = altPassiveParticleRgb;
        this.inkRgb = inkRgb;
    }

    public Identifier getTexture(EntityVariantType entityType) {
        // use minecraft's textures when possible, so we can ship fewer textures
        if (entityType == EntityVariantType.GLOW_SQUID) {
            String textureName = this.getId().equals("blue") ? "glow_squid" : this.getId();
            this.texture = new Identifier("textures/entity/squid/" + textureName + ".png");
        } else {
            String textureName = this.getId().equals("blue") ? "allay" : this.getId();
            this.texture = new Identifier("textures/entity/allay/" + textureName + ".png");
        }

        return this.texture;
    }

    public String getId() {
        return this.name().toLowerCase();
    }

    public RGB getPassiveParticleRgb() {
        return this.passiveParticleRgb;
    }

    public RGB getAltPassiveParticleRgb() {
        return this.altPassiveParticleRgb;
    }

    public RGB getInkRgb() {
        return this.inkRgb;
    }

    @Override
    public String toString() {
        return this.getId();
    }

    public static EntityColour get(String id) {
        return BY_ID.get(id);
    }

    public record RGB(float r, float g, float b) {
    }
}
