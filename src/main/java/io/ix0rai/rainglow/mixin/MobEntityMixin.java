package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantProvider;
import net.minecraft.entity.mob.MobEntity;
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
        RainglowEntity entity = RainglowEntity.get(this);
        if (entity != null) {
            RainglowColour colour = generateColour(entity);
            ((VariantProvider<RainglowColour>) this).setVariant(colour);
            cir.setReturnValue(entity.createEntityData(colour));
        }
    }

    @Unique
    private RainglowColour generateColour(RainglowEntity entity) {
        int i = random.nextInt(100);
        int rarity = Rainglow.CONFIG.getRarity(entity);

        return i >= rarity ? entity.getDefaultColour() : RainglowColour.get(Rainglow.generateRandomColourId(this.getWorld(), this.random, entity));
    }
}
