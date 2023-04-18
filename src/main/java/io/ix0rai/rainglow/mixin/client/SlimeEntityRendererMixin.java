package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlimeEntityRenderer.class)
public class SlimeEntityRendererMixin {
    @Inject(method = "getTexture*", at = @At("HEAD"), cancellable = true)
    public void getTexture(SlimeEntity entity, CallbackInfoReturnable<Identifier> cir) {
        String colour = Rainglow.getColour(RainglowEntity.SLIME, entity.getDataTracker(), entity.getRandom());

        // don't override if the colour is lime, use the default texture
        if (Rainglow.CONFIG.isEntityEnabled(RainglowEntity.SLIME) && !colour.equals(RainglowColour.LIME.getId()) || Rainglow.CONFIG.getMode().equals(RainglowMode.byId("vanilla"))) {
            Identifier texture = Rainglow.getTexture(RainglowEntity.SLIME, colour);
            cir.setReturnValue(texture != null ? texture : Rainglow.getDefaultTexture(RainglowEntity.SLIME));
        }
    }
}
