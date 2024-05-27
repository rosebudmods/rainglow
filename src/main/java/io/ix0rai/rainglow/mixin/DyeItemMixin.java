package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.item.Items.*;

@Mixin(DyeItem.class)
public class DyeItemMixin {
    @Inject(method = "useOnEntity", at = @At("TAIL"), cancellable = true)
    private void useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        String colour = getDye(stack);
        RainglowEntity entityType = RainglowEntity.get(entity);

        if (entityType != null && !Rainglow.colourUnloaded(entityType, colour)
                && Rainglow.CONFIG.isEntityEnabled(entityType)
                && !Rainglow.getColour(entityType, entity.getDataTracker(), entity.getWorld().getRandom()).getId().equals(colour)) {
            entity.getWorld().playSoundFromEntity(user, entity, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.PLAYERS, 5.0f, 1.0f);
            if (!user.getWorld().isClient()) {
                stack.decrement(1);
            }

            DataTracker tracker = entity.getDataTracker();
            tracker.set(entityType.getTrackedData(), colour);

            cir.setReturnValue(ActionResult.success(user.getWorld().isClient()));
        }
    }

    @Unique
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