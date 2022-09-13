package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.ix0rai.rainglow.Rainglow.COLOUR;

@Mixin(GlowSquidEntity.class)
public abstract class GlowSquidEntityMixin extends SquidEntity {
    protected GlowSquidEntityMixin(EntityType<? extends SquidEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        // generate random colour
        this.getDataTracker().startTracking(COLOUR, Rainglow.COLOUR_IDS.get(random.nextInt(Rainglow.COLOUR_IDS.size())));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("colour", this.getDataTracker().get(COLOUR));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.getDataTracker().set(COLOUR, nbt.getString("Colour"));
    }
}
