package io.ix0rai.rainglow.config;

import com.mojang.serialization.Codec;
import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Option;

import java.util.function.Consumer;

public class DeferredSaveOption<T> extends Option<T> {
	public T deferredValue;
	private final Consumer<T> clickCallback;

	public DeferredSaveOption(String key, TooltipSupplier<T> tooltipSupplier, OptionTextGetter<T> textGetter, Option.ValueSet<T> values, T defaultValue, Consumer<T> updateCallback, Consumer<T> clickCallback) {
		this(key, tooltipSupplier, textGetter, values, values.codec(), defaultValue, updateCallback, clickCallback);
	}

	public DeferredSaveOption(String key, TooltipSupplier<T> tooltipSupplier, OptionTextGetter<T> textGetter, Option.ValueSet<T> values, Codec<T> codec, T defaultValue, Consumer<T> updateCallback, Consumer<T> clickCallback) {
		super(key, tooltipSupplier, textGetter, values, codec, defaultValue, updateCallback);
		this.deferredValue = this.value;
		this.clickCallback = clickCallback;
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
			this.deferredValue = object;
			if (!object.equals(this.value)) {
				this.clickCallback.accept(object);
			}
			// note: update callback is called on save
		}
	}

	public static DeferredSaveOption<Boolean> createDeferredBoolean(String key, String tooltip, boolean defaultValue, Consumer<Boolean> updateCallback, Consumer<Boolean> clickCallback) {
		return new DeferredSaveOption<>(
				Rainglow.translatableTextKey(key),
				tooltip != null ? Option.constantTooltip(Rainglow.translatableText(tooltip)) : Option.emptyTooltip(),
				(text, value) -> value ? RainglowConfigScreen.YES : RainglowConfigScreen.NO,
				BOOLEAN_VALUES,
				defaultValue,
				updateCallback,
				clickCallback
		);
	}

	public static DeferredSaveOption<Integer> createDeferredRangedInt(String key, String tooltip, int defaultValue, int min, int max, Consumer<Integer> updateCallback, Consumer<Integer> clickCallback) {
		return new DeferredSaveOption<>(
				Rainglow.translatableTextKey(key),
				tooltip != null ? Option.constantTooltip(Rainglow.translatableText(tooltip)) : Option.emptyTooltip(),
				(text, value) -> Rainglow.translatableText(key + ".value", value),
				new Option.IntRangeValueSet(min, max),
				Codec.intRange(min, max),
				defaultValue,
				updateCallback,
				clickCallback
		);
	}

	public void save() {
		this.value = this.deferredValue;
		this.updateCallback.accept(this.value);
	}
}
