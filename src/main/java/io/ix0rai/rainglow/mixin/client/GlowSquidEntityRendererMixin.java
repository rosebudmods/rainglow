package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.class_10069;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.GlowSquidEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(GlowSquidEntityRenderer.class)
public class GlowSquidEntityRendererMixin {
    /**
     * @reason use the colour from the entity's NBT data for textures
     * @author ix0rai
     */
    @Inject(method = "m_vhdjjpxx", at = @At("HEAD"), cancellable = true)
    public void getTexture(class_10069 state, CallbackInfoReturnable<Identifier> cir) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    RainglowEntity.GLOW_SQUID.overrideTexture(entityUuid, cir);
                }
            }
        }
    }
}
