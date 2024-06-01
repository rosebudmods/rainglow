package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.SlimeVariantProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends Entity implements SlimeVariantProvider {
    @Shadow
    protected abstract ParticleEffect getParticles();

    protected SlimeEntityMixin(EntityType<? extends SlimeEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(RainglowEntity.SLIME.getTrackedData(), RainglowEntity.SLIME.getDefaultColour().getId());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this.getWorld(), RainglowEntity.SLIME, this.getDataTracker(), this.random);
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour.getId());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.setVariant(RainglowEntity.SLIME.readNbt(this.getWorld(), nbt, this.random));
    }

    /**
     * @reason make smaller slimes spawn with the same colour as the parent in a split
     */
    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    public boolean spawnWithParentColour(World instance, Entity entity) {
        RainglowColour colour = Rainglow.getColour(this.getWorld(), RainglowEntity.SLIME, this.getDataTracker(), this.random);
        entity.getDataTracker().set(RainglowEntity.SLIME.getTrackedData(), colour.getId());
        return this.getWorld().spawnEntity(entity);
    }

    /**
     * @author ix0rai
     * @reason change particles based on colour
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SlimeEntity;getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SlimeEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V")
            )
    )
    public void tick(CallbackInfo ci) {
        float size = this.getDimensions(this.getPose()).width();
        RainglowColour colour = Rainglow.getColour(this.getWorld(), RainglowEntity.SLIME, this.getDataTracker(), this.random);
        int index = colour.ordinal();

        for (int j = 0; j < size / 2; j ++) {
            float f = this.random.nextFloat() * 6.2831855F;
            float g = this.random.nextFloat() * 0.5F + 0.5F;
            float h = MathHelper.sin(f) * size * g;
            float k = MathHelper.cos(f) * size * g;
            // note: y velocity of 100 is a magic value
            this.getWorld().addParticle(this.getParticles(), this.getX() + (double) h, this.getY(), this.getZ() + (double) k, index, 100.0, 0.0);
        }
    }

    @Override
    public RainglowColour getVariant() {
        return Rainglow.getColour(this.getWorld(), RainglowEntity.SLIME, this.getDataTracker(), this.random);
    }

    @Override
    public void setVariant(RainglowColour colour) {
        this.getDataTracker().set(RainglowEntity.SLIME.getTrackedData(), colour.getId());
    }
}
