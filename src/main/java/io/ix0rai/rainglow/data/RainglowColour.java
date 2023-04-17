package io.ix0rai.rainglow.data;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashMap;

public enum RainglowColour {
    BLACK(new RGB(0.0F, 0.0F, 0.0F), new RGB(0.0F, 0.0F, 0.0F), new RGB(0, 0, 0), Items.BLACK_DYE),
    BLUE(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.BLUE_DYE),
    BROWN(new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(149, 59, 35), Items.BROWN_DYE), // todo particles
    CYAN(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.CYAN_DYE), // todo particles
    GRAY(new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100), Items.GRAY_DYE),
    GREEN(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0), Items.GREEN_DYE),
    INDIGO(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 1.0F), new RGB(0, 0, 200), Items.AMETHYST_SHARD),
    LIGHT_BLUE(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.LIGHT_BLUE_DYE), // todo particles
    LIGHT_GRAY(new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100), Items.LIGHT_GRAY_DYE), // todo particles
    LIME(new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0), Items.LIME_DYE), // todo particles
    MAGENTA(new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100), Items.MAGENTA_DYE), // todo particles
    ORANGE(new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0), Items.ORANGE_DYE),
    PINK(new RGB(0.6F, 0F, 0.5F), new RGB(1.0F, 0.1F, 1.0F), new RGB(200, 0, 0), Items.PINK_DYE),
    PURPLE(new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100), Items.PURPLE_DYE),
    RED(new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0), Items.RED_DYE),
    WHITE(new RGB(1.0F, 1.0F, 1.0F), new RGB(1.0F, 1.0F, 1.0F), new RGB(200, 200, 200), Items.WHITE_DYE),
    YELLOW(new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 1.0F, 0.4F), new RGB(200, 0, 0), Items.YELLOW_DYE);

    private static final HashMap<String, RainglowColour> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private Identifier texture;
    private final RGB passiveParticleRgb;
    private final RGB altPassiveParticleRgb;
    private final RGB inkRgb;
    private final Item item;

    RainglowColour(RGB passiveParticleRgb, RGB altPassiveParticleRgb, RGB inkRgb, Item item) {
        this.texture = new Identifier("textures/entity/squid/" + this.getId() + ".png");
        this.passiveParticleRgb = passiveParticleRgb;
        this.altPassiveParticleRgb = altPassiveParticleRgb;
        this.inkRgb = inkRgb;
        this.item = item;
    }

    public Identifier getTexture(RainglowEntity entityType) {
        // use minecraft's textures when possible, so we can ship fewer textures
        if (entityType == RainglowEntity.GLOW_SQUID) {
            String textureName = this.getId().equals("blue") ? "glow_squid" : this.getId();
            this.texture = new Identifier("textures/entity/squid/" + textureName + ".png");
        } else if (entityType == RainglowEntity.ALLAY) {
            String textureName = this.getId().equals("blue") ? "allay" : this.getId();
            this.texture = new Identifier("textures/entity/allay/" + textureName + ".png");
        } else {
            String textureName = this.getId().equals("lime") ? "slime" : this.getId();
            this.texture = new Identifier("textures/entity/slime/" + textureName + ".png");
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

    public Item getItem() {
        return this.item;
    }

    @Override
    public String toString() {
        return this.getId();
    }

    public static RainglowColour get(String id) {
        return BY_ID.get(id);
    }

    public record RGB(float r, float g, float b) {
    }
}
