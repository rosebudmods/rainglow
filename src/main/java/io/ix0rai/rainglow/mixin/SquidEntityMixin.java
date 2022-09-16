package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SquidEntity.class)
public abstract class SquidEntityMixin extends WaterCreatureEntity {
    protected SquidEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
        throw new UnsupportedOperationException();
    }

    @Shadow protected abstract ParticleEffect getInkParticle();
    @Shadow protected abstract Vec3d applyBodyRotations(Vec3d vec3d);
    @Shadow protected abstract SoundEvent getSquirtSound();

    /**
    * @author ix0rai
    * @reason pass custom colour index to spawnParticles
    */
    @Overwrite
    private void squirt() {
        // mostly copied from the original method
        this.playSound(this.getSquirtSound(), this.getSoundVolume(), this.getSoundPitch());
        Vec3d vec3d = this.applyBodyRotations(new Vec3d(0.0, -1.0, 0.0)).add(this.getX(), this.getY(), this.getZ());

        for(int i = 0; i < 30; ++i) {
            Vec3d vec3d2 = this.applyBodyRotations(new Vec3d(this.random.nextFloat() * 0.6 - 0.3, -1.0, this.random.nextFloat() * 0.6 - 0.3));
            Vec3d vec3d3 = vec3d2.multiply(0.3 + (this.random.nextFloat() * 2.0F));

            // send in custom colour data
            // we do some horribly cursed math to remove some precision from x pos and pass in our colour as the second decimal point
            String colour = this.dataTracker.get(Rainglow.COLOUR);
            ((ServerWorld) this.world).spawnParticles(this.getInkParticle(), (Math.round(vec3d.x * 10)) / 10D + (double) Rainglow.COLOUR_IDS.indexOf(colour) / 100D, vec3d.y + 0.5, vec3d.z, 0, vec3d3.x, vec3d3.y, vec3d3.z, 0.1);
        }
    }
}
