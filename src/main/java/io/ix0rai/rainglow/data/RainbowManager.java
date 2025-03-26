package io.ix0rai.rainglow.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.UUID;

// TODO: Might need the client environment tag, but should only run client side
public class RainbowManager {
    public static LivingEntity findEntityByUuid(ClientWorld world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUuid().equals(uuid)) {
                return (LivingEntity) entity;
            }
        }

        return null;
    }

    @SuppressWarnings("DataFlowIssue")
    public static int getRainbowColor(UUID entityUuid) {
        long worldTime = MinecraftClient.getInstance().world.getTime();

        int hashCode = entityUuid.hashCode();
        float entityOffset = (hashCode & 0x7FFFFFFF) % 1000 / 1000.0f;
        float smoothHue = getHue(worldTime, entityOffset);

        return smoothedHsvToRgb(smoothHue);
    }

    public static float getHue(long worldTime, float entityOffset) {
        // Change this value to increase or decrease transition time, lower is quicker
        final int cycleDuration = 1000;

        // Progress within cycle (0.0 to 1.0)
        float progress = (worldTime % cycleDuration) / (float)cycleDuration;
        float smoothHue = (float)(0.5 + 0.5 * Math.sin(progress * 2 * Math.PI));

        smoothHue = (smoothHue + entityOffset) % 1.0f;
        return smoothHue;
    }

    public static int smoothedHsvToRgb(float h) {
        h = h % 1.0f;
        if (h < 0) h += 1.0f;

        float hueSix = h * 6.0f;
        int hueSixCategory = (int)hueSix;
        float hueSixRemainder = hueSix - hueSixCategory;

        float p = 0.0f;
        float q = (1.0f - (hueSixRemainder));
        float t = (1.0f - ((1.0f - hueSixRemainder)));

        float r, g, b;

        switch (hueSixCategory % 6) {
            case 0: r = (float) 1.0; g = t; b = p; break;
            case 1: r = q; g = (float) 1.0; b = p; break;
            case 2: r = p; g = (float) 1.0; b = t; break;
            case 3: r = p; g = q; b = (float) 1.0; break;
            case 4: r = t; g = p; b = (float) 1.0; break;
            default: r = (float) 1.0; g = p; b = q; break;
        }

        int ri = Math.min(255, Math.max(0, (int)(r * 255)));
        int gi = Math.min(255, Math.max(0, (int)(g * 255)));
        int bi = Math.min(255, Math.max(0, (int)(b * 255)));

        return (0xFF << 24) | (ri << 16) | (gi << 8) | bi;
    }
}
