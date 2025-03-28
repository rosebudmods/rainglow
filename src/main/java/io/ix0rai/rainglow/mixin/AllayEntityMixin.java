package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.AllayVariantProvider;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin extends Entity implements AllayVariantProvider {
    @Shadow public abstract void writeCustomDataToNbt(NbtCompound nbt);

    protected AllayEntityMixin(EntityType<? extends AllayEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this.getUuid(), this.getWorld(), RainglowEntity.ALLAY);
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour.getId());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.setVariant(RainglowEntity.ALLAY.readNbt(this.getWorld(), nbt, this.random));
    }

    // triggered when an allay duplicates, to apply the same colour as parent
    @Redirect(method = "duplicate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    public boolean spawnWithColour(World instance, Entity entity) {
        RainglowColour colour = Rainglow.getColour(this.getUuid(), this.getWorld(), RainglowEntity.ALLAY);
        ((AllayVariantProvider) entity).setVariant(colour);
        return this.getWorld().spawnEntity(entity);
    }

    @Override
    public RainglowColour getVariant() {
        return Rainglow.getColour(this.getUuid(), this.getWorld(), RainglowEntity.ALLAY);
    }

    @Override
    public void setVariant(RainglowColour colour) {
        Rainglow.setColour(this, colour);
    }
}
