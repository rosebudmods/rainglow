package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.EntityColour;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.SquidInkParticle;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
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
        // protect from things from like .05 turning into .04999999999999999
        d = Math.round(d * 1000.0) / 1000.0;

        int firstDecimalPoint = (int) (Math.floor(d * 10) - Math.floor(d) * 10);
        int secondDecimalPoint = (int) (Math.floor(d * 100) - Math.floor(d) * 100) - firstDecimalPoint * 10;
        int thirdDecimalPoint = (int) (Math.floor(d * 1000) - Math.floor(d) * 1000) - secondDecimalPoint * 10 - firstDecimalPoint * 100;

        // take decimal points and use them to determine the colour
        // we preserve one decimal point of x precision, so we grab the particle index from the second and third decimal point
        int colourIndex = (int) ((secondDecimalPoint + (thirdDecimalPoint / 10.0)) * 10);

        // if we get an exception (index out of bounds), we know that the sender of the particle does not have rainglow installed or has a different config
        // this will get a few rare false positives, but that's unavoidable since we have so little context
        // when we catch an exception we use white as it looks the most normal
        EntityColour.RGB rgb;
        try {
            rgb = Rainglow.getInkRgb(colourIndex);
        } catch (IndexOutOfBoundsException ignored) {
            rgb = EntityColour.WHITE.getInkRgb();
        }

        return new SquidInkParticle(clientWorld, d, e, f, g, h, i, ColorUtil.ARGB32.getArgb(255, (int) rgb.r(), (int) rgb.g(), (int) rgb.b()), this.spriteProvider);
    }
}
