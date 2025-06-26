package io.ix0rai.rainglow.data;

import net.minecraft.client.particle.ItemBreakParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.world.ClientWorld;

/**
 * @author A5ho9999
 * Helper for creating particles because Mojang likes to torture everyone
 */
public class ParticleHelper {
    public static class CustomItemBreakParticle extends ItemBreakParticle {
        public CustomItemBreakParticle(ClientWorld world, double d, double e, double f, ItemRenderState itemRenderState) {
            super(world, d, e, f, itemRenderState);
        }
    }

    public static Particle createItemBreakParticle(ClientWorld world, double d, double e, double f, ItemRenderState itemRenderState) {
        return new CustomItemBreakParticle(world, d, e, f, itemRenderState);
    }
}
