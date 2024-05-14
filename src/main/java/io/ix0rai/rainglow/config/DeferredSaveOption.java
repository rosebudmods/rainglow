package io.ix0rai.rainglow.config;

import com.mojang.serialization.Codec;
import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.Consumer;

public class DeferredSaveOption<T> extends Option<T> {
	public T deferredValue;

	public DeferredSaveOption(String key, TooltipSupplier<T> tooltipSupplier, OptionTextGetter<T> textGetter, Option.ValueSet<T> values, T defaultValue, Consumer<T> updateCallback) {
		this(key, tooltipSupplier, textGetter, values, values.codec(), defaultValue, updateCallback);
	}

	public DeferredSaveOption(String key, TooltipSupplier<T> tooltipSupplier, OptionTextGetter<T> textGetter, Option.ValueSet<T> values, Codec<T> codec, T defaultValue, Consumer<T> updateCallback) {
		super(key, tooltipSupplier, textGetter, values, codec, defaultValue, updateCallback);
		this.deferredValue = this.value;
	}

	@Override
	public void set(T value) {
		T object = this.getValues().validate(value).orElseGet(() -> {
			System.out.println("Illegal option value " + value + " for " + this.text);
			return this.defaultValue;
		});

		if (!MinecraftClient.getInstance().isRunning()) {
			this.deferredValue = object;
		} else {
			if (!Objects.equals(this.value, object)) {
				this.deferredValue = object;
				// note: callback is called on save
			}
		}
	}

	public static DeferredSaveOption<Boolean> createDeferredBoolean(String key, String tooltip, boolean defaultValue, Consumer<Boolean> updateCallback) {
		return new DeferredSaveOption<>(
				Rainglow.translatableTextKey("config." + key),
				Option.constantTooltip(tooltip == null ? Rainglow.translatableText("tooltip." + key) : Rainglow.translatableText(tooltip)),
				(text, value) -> value ? CommonTexts.YES : CommonTexts.NO,
				BOOLEAN_VALUES,
				defaultValue,
				updateCallback
		);
	}

	public static DeferredSaveOption<Integer> createDeferredRangedInt(String key, String tooltip, int defaultValue, int min, int max, Consumer<Integer> updateCallback) {
		return new DeferredSaveOption<>(
				Rainglow.translatableTextKey("config." + key),
				Option.constantTooltip(tooltip == null ? Rainglow.translatableText("tooltip." + key) : Rainglow.translatableText(tooltip)),
				(text, value) -> Rainglow.translatableText("value." + key, value),
				new Option.IntRangeValueSet(min, max),
				Codec.intRange(min, max),
				defaultValue,
				updateCallback
		);
	}

	public static Option<Double> createDeferredRangedDouble(String key, double defaultValue, double min, double max, Consumer<Double> updateCallback) {
		return new DeferredSaveOption<>(
				"rainglow.config." + key,
				Option.constantTooltip(Text.translatable("rainglow.tooltip." + key)),
				(text, value) -> GameOptions.getGenericValueText(text, Text.translatable("rainglow.value." + key, value)),
				new Option.IntRangeValueSet((int) (min * 10), (int) (max * 10)).withModifier(i -> (double) i / 10.0, double_ -> (int) (double_ * 10.0)),
				Codec.doubleRange(min, max),
				defaultValue,
				updateCallback
		);
	}

	public void save() {
		this.value = this.deferredValue;
		this.updateCallback.accept(this.value);
	}
}
