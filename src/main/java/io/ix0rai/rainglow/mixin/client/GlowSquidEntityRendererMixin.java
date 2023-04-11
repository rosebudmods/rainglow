package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.EntityColour;
import io.ix0rai.rainglow.data.EntityVariantType;
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
        String colour = Rainglow.getColour(EntityVariantType.GLOW_SQUID, glowSquidEntity.getDataTracker(), glowSquidEntity.getRandom());

        // if the colour is blue we don't need to override the method
        // this optimises a tiny bit
        if (!colour.equals(EntityColour.BLUE.getId())) {
            Identifier texture = Rainglow.getTexture(EntityVariantType.GLOW_SQUID, colour);
            cir.setReturnValue(texture != null ? texture : Rainglow.getDefaultTexture(EntityVariantType.GLOW_SQUID));
        }
    }
}
