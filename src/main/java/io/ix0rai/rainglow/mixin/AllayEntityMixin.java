package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin extends Entity implements AllayVariantProvider
{
    private static final String COLOUR_NBT_KEY = "Colour";

    protected AllayEntityMixin(EntityType<? extends GlowSquidEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(Rainglow.getTrackedColourData(EntityVariantType.Allay), EntityGlowColour.BLUE.getId());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = Rainglow.getColour(EntityVariantType.Allay, this.getDataTracker(), this.getRandom());
        nbt.putString(COLOUR_NBT_KEY, colour);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = nbt.getString(COLOUR_NBT_KEY);

        if (Rainglow.colourUnloaded(colour)) {
            colour = Rainglow.generateRandomColourId(this.getRandom());
        }

        this.setVariant(EntityGlowColour.get(colour));
    }

    @Override
    public EntityGlowColour getVariant() {
        return EntityGlowColour.get(Rainglow.getColour(EntityVariantType.Allay, this.getDataTracker(), this.getRandom()));
    }

    @Override
    public void setVariant(EntityGlowColour colour) {
        this.getDataTracker().method_12778(Rainglow.getTrackedColourData(EntityVariantType.Allay), colour.getId());
    }

    @Mixin(MobEntity.class)
    public abstract static class MobEntityMixin extends LivingEntity {
        protected MobEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
            super(entityType, world);
        }

        @SuppressWarnings("all")
        @Inject(method = "initialize", at = @At("RETURN"), cancellable = true)
        public void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt, CallbackInfoReturnable<EntityData> cir) {
            if ((Object) this instanceof AllayEntity allay) {
                String colour = Rainglow.generateRandomColourId(this.getRandom());
                ((AllayVariantProvider) allay).setVariant(EntityGlowColour.get(colour));
                cir.setReturnValue(new AllayEntityData(EntityGlowColour.get(colour)));
            }
        }
    }

    public RandomGenerator getRandom() {
        return this.random;
    }
}
