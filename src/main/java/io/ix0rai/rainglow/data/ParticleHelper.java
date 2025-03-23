package io.ix0rai.rainglow.data;

import net.minecraft.client.particle.ItemBreakParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.unmapped.C_hvackyip;

/**
 * @author A5ho9999
 * Helper for creating particles because Mojang likes to torture everyone
 */
public class ParticleHelper {
    public static class CustomItemBreakParticle extends ItemBreakParticle {
        public CustomItemBreakParticle(ClientWorld world, double d, double e, double f, C_hvackyip c_hvackyip) {
            super(world, d, e, f, c_hvackyip);
        }
    }

    public static Particle createItemBreakParticle(ClientWorld world, double d, double e, double f, C_hvackyip c_hvackyip) {
        return new CustomItemBreakParticle(world, d, e, f, c_hvackyip);
    }
}
