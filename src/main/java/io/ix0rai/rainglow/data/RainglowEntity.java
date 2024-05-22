package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public enum RainglowEntity {
    GLOW_SQUID("glow_squid", RainglowColour.BLUE, DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING)),
    ALLAY("allay", RainglowColour.BLUE, DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.STRING)),
    SLIME("slime", RainglowColour.LIME, DataTracker.registerData(SlimeEntity.class, TrackedDataHandlerRegistry.STRING));

    private static final HashMap<String, RainglowEntity> BY_ID = new HashMap<>();
    static {
        Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private final String id;
    private final RainglowColour defaultColour;
    private final TrackedData<String> trackedData;

    RainglowEntity(String id, RainglowColour defaultColour, TrackedData<String> trackedData) {
        this.id = id;
        this.defaultColour = defaultColour;
		this.trackedData = trackedData;
	}

    public String getId() {
        return this.id;
    }

    public RainglowColour getDefaultColour() {
        return this.defaultColour;
    }

    public TrackedData<String> getTrackedData() {
        return this.trackedData;
    }

    public Identifier getDefaultTexture() {
        return this.defaultColour.getTexture(this);
    }

    public Item getItem(int index) {
        if (index == -1) {
            return this.getDefaultColour().getItem();
        }

        return RainglowColour.values()[index].getItem();
    }

    public RainglowColour readNbt(NbtCompound nbt, RandomGenerator random) {
        String colour = nbt.getString(Rainglow.CUSTOM_NBT_KEY);

        if (Rainglow.colourUnloaded(this, colour)) {
            colour = Rainglow.generateRandomColourId(random);
        }

        return RainglowColour.get(colour);
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
