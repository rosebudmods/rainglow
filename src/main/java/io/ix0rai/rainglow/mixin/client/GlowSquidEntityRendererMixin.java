package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
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
     * @reason Use the colour from the entity's NBT data
     * @author ix0rai
     */
    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    public void getTexture(GlowSquidEntity glowSquidEntity, CallbackInfoReturnable<Identifier> cir) {
        String colour = glowSquidEntity.getDataTracker().get(Rainglow.COLOUR);

        if (!colour.equals(SquidColour.BLUE.getId())) {
            Identifier texture = Rainglow.getTexture(glowSquidEntity.getDataTracker().get(Rainglow.COLOUR));
            cir.setReturnValue(texture != null ? texture : Rainglow.getDefaultTexture());
        }
    }
}
