package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.RGB;
import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowParticle.GlowFactory.class)
public class GlowParticleMixin {
    @Final
    @Shadow
    private SpriteProvider spriteProvider;

    /**
     * @author ix0rai
     * @reason recolor particles
     */
    @Inject(method = "createParticle*", at = @At("HEAD"), cancellable = true)
    public void createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<GlowParticle> cir) {
        // we use whether g is over 100 to determine if we should override the method
        if (g > 99) {
            g -= 100;
            GlowParticle glowParticle = new GlowParticle(clientWorld, d, e, f, 0.5 - GlowParticle.RANDOM.nextDouble(), h, 0.5 - GlowParticle.RANDOM.nextDouble(), this.spriteProvider);

            // we check the g value to see what the colour is
            Pair<RGB, RGB> rgbs = Rainglow.PASSIVE_PARTICLE_RGBS.get((int) g);
            if (GlowParticle.RANDOM.nextBoolean()) {
                glowParticle.setColor(rgbs.getLeft().r(), rgbs.getLeft().g(), rgbs.getLeft().b());
            } else {
                glowParticle.setColor(rgbs.getRight().r(), rgbs.getRight().g(), rgbs.getRight().b());
            }

            glowParticle.velocityY *= 0.2;
            if (g == 0.0 && i == 0.0) {
                glowParticle.velocityX *= 0.1;
                glowParticle.velocityZ *= 0.1;
            }

            glowParticle.setMaxAge((int) (8.0 / (clientWorld.random.nextDouble() * 0.8 + 0.2)));
            cir.setReturnValue(glowParticle);
        }
    }
}
