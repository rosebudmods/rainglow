package io.ix0rai.rainglow.data;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum RainglowColour {
    BLACK("black", 0x000000, new RGB(0.0F, 0.0F, 0.0F), new RGB(0.0F, 0.0F, 0.0F), new RGB(0, 0, 0), Items.BLACK_DYE),
    BLUE("blue", 0x0000FF, new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.BLUE_DYE),
    BROWN("brown", 0x964B00, new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(149, 59, 35), Items.BROWN_DYE),
    CYAN("cyan", 0x00FFFF, new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.CYAN_DYE),
    GRAY("gray", 0x808080, new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100), Items.GRAY_DYE),
    GREEN("green", 0x0A5C36, new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0), Items.GREEN_DYE),
    INDIGO("indigo", 0x4B0082, new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 1.0F), new RGB(0, 0, 200), Items.AMETHYST_SHARD),
    LIGHT_BLUE("light_blue", 0xADD8E6, new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 0.4F, 0.4F), new RGB(204, 31, 102), Items.LIGHT_BLUE_DYE),
    LIGHT_GRAY("light_gray", 0xd3d3d3, new RGB(0.6F, 0.6F, 0.6F), new RGB(0.4F, 0.4F, 0.4F), new RGB(100, 100, 100), Items.LIGHT_GRAY_DYE),
    LIME("lime", 0x32CD32, new RGB(0.6F, 1.0F, 0.8F), new RGB(0.08F, 1.0F, 0.4F), new RGB(0, 200, 0), Items.LIME_DYE),
    MAGENTA("magenta", 0xFF00FF, new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100), Items.MAGENTA_DYE),
    ORANGE("orange", 0xFFA500, new RGB(1.0F, 0.5F, 0.0F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0), Items.ORANGE_DYE),
    PINK("pink", 0xFFC0CB, new RGB(0.6F, 0F, 0.5F), new RGB(1.0F, 0.1F, 1.0F), new RGB(200, 0, 0), Items.PINK_DYE),
    PURPLE("purple", 0x800080, new RGB(0.3F, 0F, 0.25F), new RGB(0.5F, 0.05F, 0.5F), new RGB(200, 0, 100), Items.PURPLE_DYE),
    RED("red", 0xFF0000, new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 0.4F, 0.4F), new RGB(200, 0, 0), Items.RED_DYE),
    WHITE("white", 0xFFFFFF, new RGB(1.0F, 1.0F, 1.0F), new RGB(1.0F, 1.0F, 1.0F), new RGB(200, 200, 200), Items.WHITE_DYE),
    YELLOW("yellow", 0xFFFF00, new RGB(1.0F, 1.0F, 0.8F), new RGB(1.0F, 1.0F, 0.4F), new RGB(200, 0, 0), Items.YELLOW_DYE);

    private static final HashMap<String, RainglowColour> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private final String id;
    private final int hex;
    private final Map<RainglowEntity, Identifier> textures;
    private final RGB passiveParticleRgb;
    private final RGB altPassiveParticleRgb;
    private final RGB inkRgb;
    private final Item item;

    RainglowColour(String id, int hex, RGB passiveParticleRgb, RGB altPassiveParticleRgb, RGB inkRgb, Item item) {
        this.id = id;
        this.hex = hex;
        this.textures = new HashMap<>();
        this.passiveParticleRgb = passiveParticleRgb;
        this.altPassiveParticleRgb = altPassiveParticleRgb;
        this.inkRgb = inkRgb;
        this.item = item;
    }

    public Identifier getTexture(RainglowEntity entityType) {
        if (this.textures.isEmpty()) {
            for (RainglowEntity entity : RainglowEntity.values()) {
                // use minecraft's textures when possible, so we can ship fewer textures
                switch (entity) {
                    case GLOW_SQUID -> {
                        String textureName = RainglowEntity.GLOW_SQUID.getDefaultColour() == this ? "glow_squid" : this.getId();
                        this.textures.put(entity, new Identifier("textures/entity/squid/" + textureName + ".png"));
                    }
                    case ALLAY -> {
                        String textureName = RainglowEntity.ALLAY.getDefaultColour() == this ? "allay" : this.getId();
                        this.textures.put(entity, new Identifier("textures/entity/allay/" + textureName + ".png"));
                    }
                    case SLIME -> {
                        String textureName = RainglowEntity.SLIME.getDefaultColour() == this ? "slime" : this.getId();
                        this.textures.put(entity, new Identifier("textures/entity/slime/" + textureName + ".png"));
                    }
                }
            }
        }

        return this.textures.get(entityType);
    }

    public String getId() {
        return this.id;
    }

    public int getHex() {
        return this.hex;
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
