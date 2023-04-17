package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.item.Items.*;

@Mixin(DyeItem.class)
public class DyeItemMixin {
    @Inject(method = "useOnEntity", at = @At("TAIL"), cancellable = true)
    private void useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        String colour = getDye(stack);

        if (entity instanceof GlowSquidEntity glowSquid && glowSquid.isAlive() && !Rainglow.getColour(RainglowEntity.GLOW_SQUID, glowSquid.getDataTracker(), glowSquid.getRandom()).equals(colour)) {
            glowSquid.world.playSoundFromEntity(user, glowSquid, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!user.world.isClient) {
                stack.decrement(1);
            }

            DataTracker tracker = glowSquid.getDataTracker();
            tracker.set(Rainglow.getTrackedColourData(RainglowEntity.GLOW_SQUID), colour);

            cir.setReturnValue(ActionResult.success(user.world.isClient));
        } else if (entity instanceof AllayEntity allayEntity && allayEntity.isAlive() && !Rainglow.getColour(RainglowEntity.ALLAY, allayEntity.getDataTracker(), allayEntity.getRandom()).equals(colour)) {
            allayEntity.world.playSoundFromEntity(user, allayEntity, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!user.world.isClient) {
                stack.decrement(1);
            }

            DataTracker tracker = allayEntity.getDataTracker();
            tracker.set(Rainglow.getTrackedColourData(RainglowEntity.ALLAY), colour);

            cir.setReturnValue(ActionResult.success(user.world.isClient));
        } else if (entity instanceof SlimeEntity slimeEntity && slimeEntity.isAlive() && !Rainglow.getColour(RainglowEntity.SLIME, slimeEntity.getDataTracker(), slimeEntity.getRandom()).equals(colour)) {
            slimeEntity.world.playSoundFromEntity(user, slimeEntity, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!user.world.isClient) {
                stack.decrement(1);
            }

            DataTracker tracker = slimeEntity.getDataTracker();
            tracker.set(Rainglow.getTrackedColourData(RainglowEntity.SLIME), colour);

            cir.setReturnValue(ActionResult.success(user.world.isClient));
        }

        cir.setReturnValue(ActionResult.PASS);
    }

    private static String getDye(ItemStack item) {
        if (item.isOf(RED_DYE)) {
            return "red";
        } else if (item.isOf(BLUE_DYE)) {
            return "blue";
        } else if (item.isOf(CYAN_DYE)) {
            return "cyan";
        } else if (item.isOf(LIGHT_BLUE_DYE)) {
            return "light_blue";
        } else if (item.isOf(GRAY_DYE)) {
            return "gray";
        } else if (item.isOf(LIGHT_GRAY_DYE)) {
            return "light_gray";
        } else if (item.isOf(LIME_DYE)) {
            return "lime";
        } else if (item.isOf(GREEN_DYE)) {
            return "green";
        } else if (item.isOf(BLACK_DYE)) {
            return "black";
        } else if (item.isOf(BROWN_DYE)) {
            return "brown";
        } else if (item.isOf(ORANGE_DYE)) {
            return "orange";
        } else if (item.isOf(PINK_DYE)) {
            return "pink";
        } else if (item.isOf(WHITE_DYE)) {
            return "white";
        } else if (item.isOf(MAGENTA_DYE)) {
            return "magenta";
        } else if (item.isOf(PURPLE_DYE)) {
            return "purple";
        } else if (item.isOf(YELLOW_DYE)) {
            return "yellow";
        }

        return "blue";
    }
}