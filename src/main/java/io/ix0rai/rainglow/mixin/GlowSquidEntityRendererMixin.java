package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.render.entity.GlowSquidEntityRenderer;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GlowSquidEntityRenderer.class)
public class GlowSquidEntityRendererMixin {
    /**
     * @reason Use the colour from the entity's NBT data
     * @author ix0rai
     */
    @Overwrite
    public Identifier getTexture(GlowSquidEntity glowSquidEntity) {
        Identifier texture = Rainglow.getTexture(glowSquidEntity.getDataTracker().get(Rainglow.COLOUR));
        return texture != null ? texture : Rainglow.getDefaultTexture();
    }
}
