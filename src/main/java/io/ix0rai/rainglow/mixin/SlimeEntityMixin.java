package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.SlimeVariantProvider;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        RainglowColour colour = Rainglow.getColour(this);
        nbt.putString(Rainglow.CUSTOM_NBT_KEY, colour.getId());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.setVariant(RainglowEntity.SLIME.readNbt(this.getWorld(), nbt, this.random));
    }

    /**
     * @reason make smaller slimes spawn with the same colour as the parent in a split
     */
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void preserveColorOnSplit(Entity.RemovalReason reason, CallbackInfo ci) {
        SlimeEntity thisSlime = (SlimeEntity) (Object) this;
        int size = thisSlime.getSize();

        if (!thisSlime.getWorld().isClient && size > 1 && thisSlime.isDead()) {
            RainglowColour parentColor = Rainglow.getColour(thisSlime.getUuid());

            float width = thisSlime.getDimensions(thisSlime.getPose()).width();
            float halfWidth = width / 2.0F;
            int newSize = size / 2;
            Team team = thisSlime.getScoreboardTeam();

            int count = 2 + thisSlime.getRandom().nextInt(3);

            // Create multiple slimes individually while making sure it matches vanilla
            for (int i = 0; i < count; i++) {
                float offsetX = ((float) (i % 2) - 0.5F) * halfWidth;
                float offsetZ = ((float) (i / 2) - 0.5F) * halfWidth;

                //noinspection unchecked
                thisSlime.convert((EntityType<SlimeEntity>) thisSlime.getType(), new EntityConversionParameters(EntityConversionType.SPLIT_ON_DEATH, false, false, team), SpawnReason.TRIGGERED, (newSlime) -> {
                    newSlime.setSize(newSize, true);
                    newSlime.refreshPositionAndAngles(thisSlime.getX() + offsetX, thisSlime.getY() + 0.5, thisSlime.getZ() + offsetZ, thisSlime.getRandom().nextFloat() * 360.0F, 0.0F);

                    // Now that headache is done, finally set the child slime color to match the parent
                    ((SlimeVariantProvider) newSlime).setVariant(parentColor);
                });
            }

            // Don't forget this, boy was that a mistake
            super.remove(reason);
            ci.cancel();
        }
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
        RainglowColour colour = Rainglow.getColour(this);
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
        return Rainglow.getColour(this);
    }

    @Override
    public void setVariant(RainglowColour colour) {
        Rainglow.setColour(this, colour);
    }
}
