package io.ix0rai.rainglow.data;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public enum RainglowColour {
    BLACK("black", new RGB(0.0F, 0.0F, 0.0F), new RGB(0.0F, 0.0F, 0.0F), new RGB(0, 0, 0), Items.BLACK_DYE),
    BLUE("blue", new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.BLUE_DYE),
    BROWN("brown", new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(149, 59, 35), Items.BROWN_DYE),
    CYAN("cyan", new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.CYAN_DYE),
    GRAY("gray", new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100), Items.GRAY_DYE),
    GREEN("green", new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0), Items.GREEN_DYE),
    INDIGO("indigo", new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 1.0F), new RGB(0, 0, 200), Items.AMETHYST_SHARD),
    LIGHT_BLUE("light_blue", new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.LIGHT_BLUE_DYE),
    LIGHT_GRAY("light_gray", new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100), Items.LIGHT_GRAY_DYE),
    LIME("lime", new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0), Items.LIME_DYE),
    MAGENTA("magenta", new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100), Items.MAGENTA_DYE),
    ORANGE("orange", new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0), Items.ORANGE_DYE),
    PINK("pink", new RGB(0.6F, 0F, 0.5F), new RGB(1.0F, 0.1F, 1.0F), new RGB(200, 0, 0), Items.PINK_DYE),
    PURPLE("purple", new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100), Items.PURPLE_DYE),
    RED("red", new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0), Items.RED_DYE),
    WHITE("white", new RGB(1.0F, 1.0F, 1.0F), new RGB(1.0F, 1.0F, 1.0F), new RGB(200, 200, 200), Items.WHITE_DYE),
    YELLOW("yellow", new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 1.0F, 0.4F), new RGB(200, 0, 0), Items.YELLOW_DYE);

    private static final HashMap<String, RainglowColour> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private final String id;
    private Identifier texture;
    private final RGB passiveParticleRgb;
    private final RGB altPassiveParticleRgb;
    private final RGB inkRgb;
    private final Item item;

    RainglowColour(String id, RGB passiveParticleRgb, RGB altPassiveParticleRgb, RGB inkRgb, Item item) {
        this.id = id;
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
        return this.id;
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

    @Nullable
    public static RainglowColour get(String id) {
        return BY_ID.get(id);
    }

    public static RainglowColour.RGB getInkRgb(int index) {
        return RainglowColour.values()[index].getInkRgb();
    }

    public static RainglowColour.RGB getPassiveParticleRGB(int index, RandomGenerator random) {
        RainglowColour colour = RainglowColour.values()[index];
        return random.nextBoolean() ? colour.getPassiveParticleRgb() : colour.getAltPassiveParticleRgb();
    }

    public record RGB(float r, float g, float b) {
    }

    public static RainglowColour read(PacketByteBuf buf) {
        return get(buf.readString());
    }

    public static void write(PacketByteBuf buf, RainglowColour entity) {
        buf.writeString(entity.getId());
    }
}
