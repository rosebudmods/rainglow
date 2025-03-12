package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.class_10067;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.client.render.entity.feature.SlimeOverlayFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * @author A5ho9999
 * They've updated the render to display the outer and inner serperately, this could be used for some fun random combinations in the future.
 */
@Mixin(SlimeOverlayFeatureRenderer.class)
public class SlimeOverlayFeatureRendererMixin {
    /**
     * Override the outline texture for slimes, defaults back to normal if null
     */
    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/class_10067;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getOutline(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer rainglow$getOutline(Identifier defaultTexture, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, class_10067 state, float f, float g) {
        Identifier overrideTexture = getOverrideTexture(state);
        return overrideTexture != null ? RenderLayer.getOutline(overrideTexture) : RenderLayer.getOutline(SlimeEntityRenderer.TEXTURE);
    }

    /**
     * Override the translucent texture for slimes, defaults back to normal if null
     */
    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/class_10067;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer rainglow$getEntityTranslucent(Identifier defaultTexture, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, class_10067 state, float f, float g) {
        Identifier overrideTexture = getOverrideTexture(state);
        return overrideTexture != null ? RenderLayer.getEntityTranslucent(overrideTexture) : RenderLayer.getEntityTranslucent(SlimeEntityRenderer.TEXTURE);
    }

    @Unique
    private Identifier getOverrideTexture(class_10067 state) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    try {
                        return RainglowEntity.SLIME.overrideTexture(entityUuid);
                    } catch (Exception e) {
                        // ignore any errors and just let return null for default textures
                    }
                }
            }
        }
        return null;
    }
}
