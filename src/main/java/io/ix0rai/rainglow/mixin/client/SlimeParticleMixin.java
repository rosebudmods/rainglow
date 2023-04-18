package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.particle.ItemBreakParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DefaultParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBreakParticle.SlimeballFactory.class)
public class SlimeParticleMixin {
    /**
     * @author ix0rai
     * @reason recolor particles
     */
    @Inject(method = "createParticle*", at = @At("HEAD"), cancellable = true)
    public void createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        if (!Rainglow.CONFIG.isEntityEnabled(RainglowEntity.SLIME)) {
            cir.setReturnValue(new ItemBreakParticle(clientWorld, d, e, f, new ItemStack(Items.SLIME_BALL)));
        // 99.9d and 100.1d are used to account for floating point errors
        } else if (h >= 99.9d && h <= 100.1d) {
            ItemStack stack = Rainglow.getItem((int) g).getDefaultStack();
            cir.setReturnValue(new ItemBreakParticle(clientWorld, d, e, f, stack));
        } else {
            cir.setReturnValue(null);
        }
    }
}
