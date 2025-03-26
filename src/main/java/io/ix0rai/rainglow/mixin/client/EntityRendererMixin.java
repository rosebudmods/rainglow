package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import net.minecraft.class_10017;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends class_10017> {
    @Inject(method = "method_62354", at = @At("HEAD"))
    private void updateRenderState(T entity, S state, float f, CallbackInfo ci) {
        if (state instanceof EntityRenderStateTracker) {
            ((EntityRenderStateTracker) state).rainglow$setEntity(entity);

            // TODO: This is a placeholder, better to add a new variant.
            if (entity.hasCustomName()) {
                ((EntityRenderStateTracker) state).rainglow$setRainbow(entity.getCustomName().getString().contains("rainbow"));
            } else {
                // Reset it just in case
                ((EntityRenderStateTracker) state).rainglow$setRainbow(false);
            }
        }
    }
}
