package io.ix0rai.rainglow.mixin;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.EntityVariantType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyeItem.class)
public class DyeItemMixin {

    @Inject(method = "useOnEntity", at = @At("TAIL"), cancellable = true)
    private void Inject(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        String handColour = getStackColour(stack.getItem());

        GlowSquidEntity glowSquidEntity;
        AllayEntity allayEntity;
        if (entity instanceof GlowSquidEntity && (glowSquidEntity = (GlowSquidEntity) entity).isAlive() && !Rainglow.getColour(EntityVariantType.GlowSquid, glowSquidEntity.getDataTracker(), glowSquidEntity.getRandom()).equals(handColour))
        {
            glowSquidEntity.world.playSoundFromEntity(user, glowSquidEntity, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!user.world.isClient) stack.decrement(1);

            DataTracker tracker = glowSquidEntity.getDataTracker();
            tracker.method_12778(Rainglow.getTrackedColourData(EntityVariantType.GlowSquid), handColour);

            cir.setReturnValue(ActionResult.success(user.world.isClient));
        }
        else if (entity instanceof AllayEntity && (allayEntity = (AllayEntity) entity).isAlive() & !Rainglow.getColour(EntityVariantType.Allay, allayEntity.getDataTracker(), allayEntity.getRandom()).equals(handColour))
        {
            allayEntity.world.playSoundFromEntity(user, allayEntity, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
            if (!user.world.isClient) stack.decrement(1);

            DataTracker tracker = allayEntity.getDataTracker();
            tracker.method_12778(Rainglow.getTrackedColourData(EntityVariantType.Allay), handColour);

            cir.setReturnValue(ActionResult.success(user.world.isClient));
        }

        cir.setReturnValue(ActionResult.PASS);
    }

    private static String getStackColour(Item item) {
        switch (item.getName().getString()) {
            case "Red Dye" -> {
                return "red";
            }
            case "Blue Dye", "Cyan Dye", "Light Blue Dye" -> {
                return "blue";
            }
            case "Gray Dye", "Light Gray Dye" -> {
                return "gray";
            }
            case "Lime Dye", "Green Dye" -> {
                return "green";
            }
            case "Black Dye" -> {
                return "black";
            }
            case "Brown Dye", "Orange Dye" -> {
                return "orange";
            }
            case "Pink Dye" -> {
                return "pink";
            }
            case "White Dye" -> {
                return "white";
            }
            case "Magenta Dye" -> {
                return "indigo";
            }
            case "Purple Dye" -> {
                return "purple";
            }
            case "Yellow Dye" -> {
                return "yellow";
            }
        }
        return "blue";
    }
}