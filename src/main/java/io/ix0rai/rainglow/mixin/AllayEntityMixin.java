package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.AllayVariantProvider;
import io.ix0rai.rainglow.data.EntityColour;
import io.ix0rai.rainglow.data.EntityVariantType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin extends Entity implements AllayVariantProvider {
    @Shadow
    public long field_39471;
    @SuppressWarnings("WrongEntityDataParameterClass")
    @Final @Shadow
    private static final TrackedData<Boolean> CAN_DUPLICATE = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected AllayEntityMixin(EntityType<? extends AllayEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(Rainglow.getTrackedColourData(EntityVariantType.ALLAY), EntityColour.BLUE.getId());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = Rainglow.getColour(EntityVariantType.ALLAY, this.getDataTracker(), this.getRandom());
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = nbt.getString(Rainglow.CUSTOM_NBT_KEY);

        if (Rainglow.colourUnloaded(colour)) {
            colour = Rainglow.generateRandomColourId(this.getRandom());
        }

        this.setVariant(EntityColour.get(colour));
    }

    // Triggered when Duplicate Allay is cloned, apply same colour
    @Inject(method = "method_44363", at = @At("HEAD"), cancellable = true)
    public void createDuplicate(CallbackInfo ci) {
        AllayEntity allayEntity = EntityType.ALLAY.create(this.world);
        if (allayEntity != null) {
            allayEntity.refreshPositionAfterTeleport(this.getPos());
            allayEntity.setPersistent();
            method_44364(allayEntity);
            this.method_44364(null);

            // TODO: Config option to disable "Allay Cloned Colour"
            EntityColour colour = EntityColour.get(Rainglow.getColour(EntityVariantType.ALLAY, this.getDataTracker(), this.getRandom()));
            allayEntity.getDataTracker().set(Rainglow.getTrackedColourData(EntityVariantType.ALLAY), colour.getId());

            this.world.spawnEntity(allayEntity);
        }
        ci.cancel();
    }

    // TODO: This could probably be better?
    private void method_44364(AllayEntity allayEntity) {
        if (allayEntity == null) {
            this.field_39471 = 6000L;
            this.dataTracker.set(CAN_DUPLICATE, false);
        }
        else {
            allayEntity.field_39471 = 6000L;
            allayEntity.getDataTracker().set(CAN_DUPLICATE, false);
        }
    }


    @Override
    public EntityColour getVariant() {
        return EntityColour.get(Rainglow.getColour(EntityVariantType.ALLAY, this.getDataTracker(), this.getRandom()));
    }

    @Override
    public void setVariant(EntityColour colour) {
        this.getDataTracker().set(Rainglow.getTrackedColourData(EntityVariantType.ALLAY), colour.getId());
    }

    public RandomGenerator getRandom() {
        return this.random;
    }
}
