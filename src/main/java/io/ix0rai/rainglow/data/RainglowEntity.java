package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public enum RainglowEntity {
    GLOW_SQUID("glow_squid", RainglowColour.BLUE, GlowSquidEntityData::new),
    ALLAY("allay", RainglowColour.BLUE, AllayEntityData::new),
    SLIME("slime", RainglowColour.LIME, SlimeEntityData::new);

    private static final HashMap<String, RainglowEntity> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private final String id;
    private final RainglowColour defaultColour;
    private final Function<RainglowColour, EntityData> entityDataFactory;

    RainglowEntity(String id, RainglowColour defaultColour, Function<RainglowColour, EntityData> entityDataFactory) {
        this.id = id;
        this.defaultColour = defaultColour;
		this.entityDataFactory = entityDataFactory;
	}

    public String getId() {
        return this.id;
    }

    public RainglowColour getDefaultColour() {
        return this.defaultColour;
    }

    public Identifier getDefaultTexture() {
        return this.defaultColour.getTexture(this);
    }

    public EntityData createEntityData(RainglowColour colour) {
        return this.entityDataFactory.apply(colour);
    }

    public Item getItem(int index) {
        if (index == -1) {
            return this.getDefaultColour().getItem();
        }

        return RainglowColour.values()[index].getItem();
    }

    public RainglowColour readNbt(World world, NbtCompound nbt, RandomGenerator random) {
        RainglowColour colour = RainglowColour.get(nbt.getString(Rainglow.CUSTOM_NBT_KEY));

        if (Rainglow.colourUnloaded(world, this, colour)) {
            colour = Rainglow.generateRandomColour(world, random);
        }

        return colour;
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

    @SuppressWarnings("all")
    public static RainglowEntity get(Entity entity) {
        if (entity instanceof GlowSquidEntity) {
            return GLOW_SQUID;
        } else if (entity instanceof AllayEntity) {
            return ALLAY;
        } else if (entity instanceof SlimeEntity) {
            return SLIME;
        }

        return null;
    }

    @Nullable
    public Identifier overrideTexture(UUID uuid, World world) {
        RainglowColour colour = Rainglow.getColour(uuid, world, this);

        // Returning null will just use default texture, no need for extra checks

        // if the colour is default we don't need to override the method
        // this optimises a tiny bit
        if (Rainglow.CONFIG.isEntityEnabled(this) && colour != this.getDefaultColour()) {
            return colour.getTexture(this);
        }

        return null;
    }
}
