package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.class_10067;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(SlimeEntityRenderer.class)
public class SlimeEntityRendererMixin {
    @Inject(method = "m_vhdjjpxx", at = @At("HEAD"), cancellable = true)
    public void getTexture(class_10067 state, CallbackInfoReturnable<Identifier> cir) {
        if (state instanceof EntityRenderStateTracker) {
            UUID entityUuid = ((EntityRenderStateTracker) state).rainglow$getEntityUuid();
            if (entityUuid != null) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    boolean rainbowState = ((EntityRenderStateTracker) state).rainglow$isRainbow();

                    RainglowEntity type = RainglowEntity.SLIME;
                    Identifier texture = type.overrideTexture(world, entityUuid, rainbowState);
                    cir.setReturnValue(texture != null ? texture : type.getDefaultTexture());
                }
            }
        }
    }
}
