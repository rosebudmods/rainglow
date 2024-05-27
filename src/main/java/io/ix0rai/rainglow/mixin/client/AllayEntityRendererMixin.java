package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.render.entity.AllayEntityRenderer;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AllayEntityRenderer.class)
public class AllayEntityRendererMixin {
    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    public void getTexture(AllayEntity allayEntity, CallbackInfoReturnable<Identifier> cir) {
        RainglowEntity.ALLAY.overrideTexture(allayEntity, cir);
    }
}
