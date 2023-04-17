package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.AllayVariantProvider;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin extends Entity implements AllayVariantProvider {
    protected AllayEntityMixin(EntityType<? extends AllayEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(Rainglow.getTrackedColourData(RainglowEntity.ALLAY), RainglowColour.BLUE.getId());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = Rainglow.getColour(RainglowEntity.ALLAY, this.getDataTracker(), this.getRandom());
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = nbt.getString(Rainglow.CUSTOM_NBT_KEY);

        if (Rainglow.colourUnloaded(colour)) {
            colour = Rainglow.generateRandomColourId(this.getRandom());
        }

        this.setVariant(RainglowColour.get(colour));
    }

    // triggered when an allay duplicates, to apply the same colour as parent
    @Redirect(method = "method_44363", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    public boolean spawnWithColour(World instance, Entity entity) {
        RainglowColour colour = RainglowColour.get(Rainglow.getColour(RainglowEntity.ALLAY, this.getDataTracker(), this.getRandom()));
        entity.getDataTracker().set(Rainglow.getTrackedColourData(RainglowEntity.ALLAY), colour.getId());
        return this.world.spawnEntity(entity);
    }

    @Override
    public RainglowColour getVariant() {
        return RainglowColour.get(Rainglow.getColour(RainglowEntity.ALLAY, this.getDataTracker(), this.getRandom()));
    }

    @Override
    public void setVariant(RainglowColour colour) {
        this.getDataTracker().set(Rainglow.getTrackedColourData(RainglowEntity.ALLAY), colour.getId());
    }

    public RandomGenerator getRandom() {
        return this.random;
    }
}
