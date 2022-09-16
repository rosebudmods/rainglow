package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.RGB;
import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.SquidInkParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SquidInkParticle.GlowSquidInkFactory.class)
public class SquidInkParticleMixin {
    @Final
    @Shadow
    private SpriteProvider spriteProvider;

    /**
     * @author ix0rai
     * @reason custom colours for ink particles
     */
    @Overwrite
    public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
        int firstDecimalPoint = (int) (Math.floor(d * 10) - Math.floor(d) * 10);
        int secondDecimalPoint = (int) (Math.floor(d * 100) - Math.floor(d) * 100) - firstDecimalPoint * 10;

        RGB rgb = Rainglow.INK_PARTICLE_RGB.get(secondDecimalPoint);
        return new SquidInkParticle(clientWorld, d, e, f, g, h, i, ColorHelper.Argb.getArgb(255, (int) rgb.r(), (int) rgb.g(), (int) rgb.b()), this.spriteProvider);
    }
}
