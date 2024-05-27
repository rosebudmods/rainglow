package io.ix0rai.rainglow.mixin.client.screen;

import com.llamalad7.mixinextras.sugar.Local;
import io.ix0rai.rainglow.config.DeferredSaveOption;
import net.minecraft.client.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Option.CyclingValueSet.class)
public interface CyclingValueSetMixin {
	@ModifyArg(
			method = "method_42723",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/widget/button/CyclingButtonWidget$Builder;initially(Ljava/lang/Object;)Lnet/minecraft/client/gui/widget/button/CyclingButtonWidget$Builder;"
					),
			index = 0
	)
	private <T> T updateOptionValue(T value, @Local(argsOnly = true) Option<T> option) {
		return option instanceof DeferredSaveOption<T> deferred ? deferred.deferredValue : value;
	}
}
