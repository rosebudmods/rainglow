package io.ix0rai.rainglow;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.random.RandomGenerator;

/**
 * you may be wondering why this class exists
 * well, it's because the data tracker is a static field
 * we can't define it from a mixin, because both {@link io.ix0rai.rainglow.mixin.GlowSquidEntityMixin} and {@link io.ix0rai.rainglow.mixin.client.GlowSquidEntityRendererMixin} need to access it,
 * so we define it here and access it from the mixins
 * <p>
 * now you may be wondering why it gets an entire class to itself
 * this is because initialising the static field on mod init causes issues,
 * so we have to give it its own class so that it's not loaded before it's needed
 * <p>
 * I am 100% aware this is stupid, but it's the only way I could get it to work
 */
public class ColourData {
    public static final TrackedData<String> COLOUR = DataTracker.registerData(GlowSquidEntity.class, TrackedDataHandlerRegistry.STRING);

    public static String getColour(DataTracker tracker, RandomGenerator random) {
        // generate random colour if the squid's colour isn't currently loaded
        String colour = tracker.get(COLOUR);
        if (!Rainglow.isColourLoaded(colour)) {
            tracker.set(COLOUR, Rainglow.generateRandomColour(random).getId());
            colour = tracker.get(COLOUR);
        }

        return colour;
    }
}
