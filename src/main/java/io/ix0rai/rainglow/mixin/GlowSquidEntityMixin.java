package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.ColourData;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
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
public abstract class GlowSquidEntityMixin extends SquidEntity {
    private static final String COLOUR_KEY = "Colour";

    protected GlowSquidEntityMixin(EntityType<? extends SquidEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        // generate random colour
        this.getDataTracker().startTracking(ColourData.COLOUR, Rainglow.generateRandomColour(random).getId());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString(COLOUR_KEY, this.getDataTracker().get(ColourData.COLOUR));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = nbt.getString(COLOUR_KEY);
        // if read colour does not exist in the colour map, generate the squid a new one
        if (Rainglow.isColourLoaded(colour)) {
            this.getDataTracker().set(ColourData.COLOUR, colour);
        } else {
            this.getDataTracker().set(ColourData.COLOUR, Rainglow.generateRandomColour(random).getId());
        }
    }

    /**
     * @author ix0rai
     * @reason change particles based on colour
     */
    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), cancellable = true)
    public void tickMovement(CallbackInfo ci) {
        String colour = ColourData.getColour(dataTracker, this.random);
        if (!colour.equals(SquidColour.BLUE.getId())) {
            // we add 100 to g to let the mixin know that we want to override the method
            this.world.addParticle(ParticleTypes.GLOW, this.getParticleX(0.6), this.getRandomBodyY(), this.getParticleZ(0.6), Rainglow.getColourIndex(colour) + 100, 0, 0);
            ci.cancel();
        }
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
                String colour = ColourData.getColour(this.dataTracker, this.random);
                int index = Rainglow.getColourIndex(colour);
                // round x to 1 decimal place and append index data to the next two
                return ((ServerWorld) this.world).spawnParticles(particle, (Math.round(x * 10)) / 10D + index / 1000D, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
            } else {
                // normal logic for squid
                return ((ServerWorld) this.world).spawnParticles(particle, x, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
            }
        }
    }
}
