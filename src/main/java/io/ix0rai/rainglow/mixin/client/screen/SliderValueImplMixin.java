package io.ix0rai.rainglow.mixin.client.screen;

import com.llamalad7.mixinextras.sugar.Local;
import io.ix0rai.rainglow.config.DeferredSaveOption;
import net.minecraft.client.option.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Option.OptionSliderWidgetImpl.class)
public class SliderValueImplMixin {
	@ModifyArg(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/option/Option$SliderValueSet;toSliderValue(Ljava/lang/Object;)D"
			),
			index = 0
	)
	private static <T> T updateOptionValue(T value, @Local(argsOnly = true) Option<T> option) {
		return option instanceof DeferredSaveOption<T> deferred ? deferred.deferredValue : value;
	}
}
