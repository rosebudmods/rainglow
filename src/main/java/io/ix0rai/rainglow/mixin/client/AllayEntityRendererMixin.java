package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.class_9996;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.AllayEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AllayEntityRenderer.class)
public class AllayEntityRendererMixin {
    @Inject(method = "m_vhdjjpxx", at = @At("HEAD"), cancellable = true)
    public void getTexture(class_9996 state, CallbackInfoReturnable<Identifier> cir) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    RainglowEntity.ALLAY.overrideTexture(entityUuid, cir);
                }
            }
        }
    }
}
