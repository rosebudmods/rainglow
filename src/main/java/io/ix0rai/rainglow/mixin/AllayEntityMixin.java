package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.EntityVariantProvider;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.world.World;
import net.minecraft.world.storage.DataReader;
import net.minecraft.world.storage.DataWriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AllayEntity.class)
public abstract class AllayEntityMixin extends Entity implements EntityVariantProvider {
    protected AllayEntityMixin(EntityType<? extends AllayEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    public void writeCustomData(DataWriter writer, CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this.getUuid(), this.getWorld(), RainglowEntity.ALLAY);
        writer.writeString(Rainglow.CUSTOM_NBT_KEY, colour.getId());
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    public void readCustomData(DataReader reader, CallbackInfo ci) {
        this.rainglow$setVariant(RainglowEntity.ALLAY.readData(this.getWorld(), reader, this.random));
    }

    // triggered when an allay duplicates, to apply the same colour as parent
    @Redirect(method = "duplicate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    public boolean spawnWithColour(World instance, Entity entity) {
        RainglowColour colour = Rainglow.getColour(this.getUuid(), this.getWorld(), RainglowEntity.ALLAY);
        ((EntityVariantProvider) entity).rainglow$setVariant(colour);
        return this.getWorld().spawnEntity(entity);
    }

    @Override
    @Unique
    public RainglowColour rainglow$getVariant() {
        return Rainglow.getColour(this.getUuid(), this.getWorld(), RainglowEntity.ALLAY);
    }

    @Override
    @Unique
    public void rainglow$setVariant(RainglowColour colour) {
        Rainglow.setColour(this, colour);
    }
}
