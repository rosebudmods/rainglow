package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.SquidColour;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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
    private static final String COLOUR_NBT_KEY = "Colour";
    private static String lastGeneratedColour = null;
    private static EnvType lastEnvType = null;

    protected GlowSquidEntityMixin(EntityType<? extends SquidEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        // so.
        // huge hack here
        // in single player, when you spawn an entity, it will create TWO SEPARATE ENTITIES
        // one on the server and one on the client
        // this means that they will have separate data trackers, which means that the client data tracker can generate a different colour from the server
        // this is bad, because the client squid will appear the wrong colour
        // we hack around this by generating the colour on one side, and then using that colour on the other side
        if (lastGeneratedColour == null) {
            String colour = Rainglow.generateRandomColourId(this.getRandom());
            this.getDataTracker().startTracking(Rainglow.getTrackedColourData(), colour);
            lastGeneratedColour = colour;
        } else if (lastEnvType != FabricLoader.getInstance().getEnvironmentType()) {
            this.getDataTracker().startTracking(Rainglow.getTrackedColourData(), lastGeneratedColour);
            lastGeneratedColour = null;
        } else {
            // if we're on the same side as the last squid, we know our hack doesn't apply
            this.getDataTracker().startTracking(Rainglow.getTrackedColourData(), Rainglow.generateRandomColourId(this.getRandom()));
        }

        lastEnvType = FabricLoader.getInstance().getEnvironmentType();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = Rainglow.getColour(this.getDataTracker(), this.getRandom());
        nbt.putString(COLOUR_NBT_KEY, colour);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        String colour = nbt.getString(COLOUR_NBT_KEY);

        // if read colour does not exist in the colour map, generate the squid a new one
        if (Rainglow.colourUnloaded(colour)) {
            colour = Rainglow.generateRandomColourId(this.getRandom());
        }

        this.getDataTracker().set(Rainglow.getTrackedColourData(), colour);
    }

    /**
     * @author ix0rai
     * @reason change particles based on colour
     */
    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), cancellable = true)
    public void tickMovement(CallbackInfo ci) {
        String colour = Rainglow.getColour(this.getDataTracker(), this.getRandom());
        if (!colour.equals(SquidColour.BLUE.getId())) {
            // we add 100 to g to let the mixin know that we want to override the method
            this.getWorld().addParticle(ParticleTypes.GLOW, this.getParticleX(0.6), this.getRandomBodyY(), this.getParticleZ(0.6), Rainglow.getColourIndex(colour) + 100, 0, 0);
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
                String colour = Rainglow.getColour(this.getDataTracker(), this.getRandom());
                int index = Rainglow.getColourIndex(colour);
                // round x to 1 decimal place and append index data to the next two
                return ((ServerWorld) this.getWorld()).spawnParticles(particle, (Math.round(x * 10)) / 10D + index / 1000D, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
            } else {
                // normal logic for squid
                return ((ServerWorld) this.getWorld()).spawnParticles(particle, x, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
            }
        }
    }
}
