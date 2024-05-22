package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.client.render.entity.GlowSquidEntityRenderer;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowSquidEntityRenderer.class)
public class GlowSquidEntityRendererMixin {
    /**
     * @reason use the colour from the entity's NBT data for textures
     * @author ix0rai
     */
    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    public void getTexture(GlowSquidEntity glowSquidEntity, CallbackInfoReturnable<Identifier> cir) {
        RainglowColour colour = Rainglow.getColour(RainglowEntity.GLOW_SQUID, glowSquidEntity.getDataTracker(), glowSquidEntity.getRandom());

        // if the colour is blue we don't need to override the method
        // this optimises a tiny bit
        if (Rainglow.CONFIG.isEntityEnabled(RainglowEntity.GLOW_SQUID) && colour != RainglowEntity.GLOW_SQUID.getDefaultColour()) {
            Identifier texture = Rainglow.getTexture(RainglowEntity.GLOW_SQUID, colour.getId());
            cir.setReturnValue(texture != null ? texture : RainglowEntity.GLOW_SQUID.getDefaultTexture());
        }
    }
}
