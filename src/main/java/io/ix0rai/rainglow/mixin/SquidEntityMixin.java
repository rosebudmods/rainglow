package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SquidEntity.class)
public abstract class SquidEntityMixin extends WaterCreatureEntity {
    protected SquidEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    /**
     * @author ix0rai
     * @reason pass custom colour index to spawnParticles
     *
     * @return value of spawnParticles
     */
    @Redirect(method = "squirt", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
    private int spawnParticles(ServerWorld instance, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        try {
            // send in custom colour data
            String colour = Rainglow.getColour(this.dataTracker, this.random);
            int index = Rainglow.getColourIndex(colour);
            // round x to 1 decimal place and append index data to the next two
            return ((ServerWorld) this.world).spawnParticles(particle, (Math.round(x * 10)) / 10D + index / 1000D, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
        } catch (Exception e) {
            // if colour tracker data is not present do not try to send it
            // this behaviour will occur when a normal squid squirts
            return ((ServerWorld) this.world).spawnParticles(particle, x, y + 0.5, z, 0, deltaX, deltaY, deltaZ, speed);
        }
    }
}
