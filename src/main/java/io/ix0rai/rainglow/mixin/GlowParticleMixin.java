package io.ix0rai.rainglow.mixin;

import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlowParticle.GlowFactory.class)
public class GlowParticleMixin {
    @Final
    @Shadow
    private SpriteProvider spriteProvider;

    /**
     * @author ix0rai
     * @reason recolor particles
     */
    @Overwrite
    public Particle createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
        GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.5 - GlowParticle.RANDOM.nextDouble(), h, 0.5 - GlowParticle.RANDOM.nextDouble(), this.spriteProvider);

        // we check the g value to see what the colour is
        // todo: sync with indexes in colour list
        if (g == 1.0) {
            if (clientWorld.random.nextBoolean()) {
                glowParticle.setColor(1.0F, 1.0F, 0.8F);
            } else {
                glowParticle.setColor(1.0F, 0.4F, 0.4F);
            }
        } else {
            if (clientWorld.random.nextBoolean()) {
                glowParticle.setColor(0.6F, 1.0F, 0.8F);
            } else {
                glowParticle.setColor(0.08F, 0.4F, 0.4F);
            }
        }

        glowParticle.velocityY *= 1.0;
        if (g == 0.0 && i == 0.0) {
            glowParticle.velocityX *= 1.0;
            glowParticle.velocityZ *= 1.0;
        }

        glowParticle.setMaxAge((int)(8.0 / (clientWorld.random.nextDouble() * 0.8 + 0.2)));
        return glowParticle;
    }
}
