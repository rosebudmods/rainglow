package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.AllayEntityData;
import io.ix0rai.rainglow.data.AllayVariantProvider;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.GlowSquidEntityData;
import io.ix0rai.rainglow.data.GlowSquidVariantProvider;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.SlimeEntityData;
import io.ix0rai.rainglow.data.SlimeVariantProvider;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("all")
    @Inject(method = "initialize", at = @At("RETURN"), cancellable = true)
    public void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        if ((Object) this instanceof GlowSquidEntity glowSquid) {
            String colour = Rainglow.generateRandomColourId(this.getRandom());
            ((GlowSquidVariantProvider) glowSquid).setVariant(getColourOrDefault(this.random, RainglowColour.BLUE, colour));
            cir.setReturnValue(new GlowSquidEntityData(getColourOrDefault(this.random, RainglowColour.BLUE, colour)));
        } else if ((Object) this instanceof AllayEntity allay) {
            String colour = Rainglow.generateRandomColourId(this.getRandom());
            ((AllayVariantProvider) allay).setVariant(getColourOrDefault(this.random, RainglowColour.BLUE, colour));
            cir.setReturnValue(new AllayEntityData(getColourOrDefault(this.random, RainglowColour.BLUE, colour)));
        } else if ((Object) this instanceof SlimeEntity slime) {
            String colour = Rainglow.generateRandomColourId(this.getRandom());
            ((SlimeVariantProvider) slime).setVariant(getColourOrDefault(this.random, RainglowColour.LIME, colour));
            cir.setReturnValue(new SlimeEntityData(getColourOrDefault(this.random, RainglowColour.LIME, colour)));
        }
    }

    @Unique
    private RainglowColour getColourOrDefault(RandomGenerator random, RainglowColour defaultColour, String randomColour) {
        return random.nextInt(100) >= Rainglow.CONFIG.getRarity(this.getCurrentEntity()) ? defaultColour : RainglowColour.get(randomColour);
    }

    @Unique
    @SuppressWarnings("all")
    private RainglowEntity getCurrentEntity() {
        if ((Object) this instanceof GlowSquidEntity) {
            return RainglowEntity.GLOW_SQUID;
        } else if ((Object) this instanceof AllayEntity) {
            return RainglowEntity.ALLAY;
        } else if ((Object) this instanceof SlimeEntity) {
            return RainglowEntity.SLIME;
        } else {
            throw new RuntimeException("unsupported entity");
        }
    }
}
