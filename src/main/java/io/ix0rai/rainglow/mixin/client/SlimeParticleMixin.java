package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.ParticleHelper;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ItemBreakParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.json.ModelTransformationMode;
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
     * @author ix0rai, A5ho9999
     * @reason recolor particles
     */
    @Inject(method = "createParticle*", at = @At("HEAD"), cancellable = true)
    public void createParticle(DefaultParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        ItemRenderState itemRenderState = new ItemRenderState();

        if (!Rainglow.CONFIG.isEntityEnabled(RainglowEntity.SLIME)) {
            MinecraftClient.getInstance().method_65386().update(itemRenderState, new ItemStack(Items.SLIME_BALL), ModelTransformationMode.GROUND, clientWorld, null, 0);
            cir.setReturnValue(ParticleHelper.createItemBreakParticle(clientWorld, d, e, f, itemRenderState));
        } else if (h >= 99.9d && h <= 100.1d) { // 99.9d and 100.1d are used to account for floating point errors
            ItemStack stack = RainglowEntity.SLIME.getItem((int) g).getDefaultStack();
            MinecraftClient.getInstance().method_65386().update(itemRenderState, stack, ModelTransformationMode.GROUND, clientWorld, null, 0);
            cir.setReturnValue(ParticleHelper.createItemBreakParticle(clientWorld, d, e, f, itemRenderState));
        } else {
            cir.setReturnValue(null);
        }
    }

}
