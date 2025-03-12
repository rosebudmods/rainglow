package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.GlowSquidVariantProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WaterCreatureEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlowSquidEntity.class)
public abstract class GlowSquidEntityMixin extends SquidEntity implements GlowSquidVariantProvider {
    protected GlowSquidEntityMixin(EntityType<? extends SquidEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this);
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour.getId());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.setVariant(RainglowEntity.GLOW_SQUID.readNbt(this.getWorld(), nbt, this.random));
    }

    /**
     * @author ix0rai
     * @reason change particles based on colour
     */
    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), cancellable = true)
    public void tickMovement(CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this);

        if (colour != RainglowColour.BLUE) {
            // we add 100 to g to let the mixin know that we want to override the method
            this.getWorld().addParticle(ParticleTypes.GLOW, this.getParticleX(0.6), this.getRandomBodyY(), this.getParticleZ(0.6), colour.ordinal() + 100, 0, 0);
            ci.cancel();
        }
    }

    @Override
    public RainglowColour getVariant() {
        return Rainglow.getColour(this);
    }

    @Override
    public void setVariant(RainglowColour colour) {
        Rainglow.setColour(this, colour);
    }

    @Mixin(SquidEntity.class)
    public abstract static class SquidEntityMixin extends WaterCreatureEntity {
        protected SquidEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
            super(entityType, world);
        }

        /**
         * @author ix0rai
         * @reason pass custom colour index to spawnParticles
         * @implNote we suppress the constant condition warning because intellij doesn't understand mixins
         * @return value of spawnParticles
         */
        @SuppressWarnings("ConstantConditions")
        @Redirect(method = "squirt", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
        private int spawnParticles(ServerWorld instance, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
            if (((Object) this) instanceof GlowSquidEntity) {
                // send in custom colour data
                RainglowColour colour = Rainglow.getColour(this);
                int index = colour.ordinal();
                // round x to 1 decimal place and append index data to the next two
                return ((ServerWorld) this.getWorld()).spawnParticles(particle, (Math.round(x * 10)) / 10D + index / 1000D, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
            } else {
                // normal logic for squid
                return ((ServerWorld) this.getWorld()).spawnParticles(particle, x, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
            }
        }
    }
}
