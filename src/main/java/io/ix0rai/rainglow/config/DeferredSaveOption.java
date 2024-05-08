package io.ix0rai.rainglow.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.Consumer;

public class DeferredSaveOption<T> extends Option<T> {
	private T deferredValue;

	public DeferredSaveOption(String key, TooltipSupplier<T> tooltipSupplier, OptionTextGetter<T> textGetter, Option.ValueSet<T> values, T defaultValue, Consumer<T> updateCallback) {
		super(key, tooltipSupplier, textGetter, values, defaultValue, updateCallback);
		this.deferredValue = this.value;
	}

	@Override
	public void set(T value) {
		T object = (T) this.getValues().validate(value).orElseGet(() -> {
			System.out.println("Illegal option value " + value + " for " + this.text);
			return this.defaultValue;
		});
		if (!MinecraftClient.getInstance().isRunning()) {
			this.deferredValue = object;
		} else {
			if (!Objects.equals(this.value, object)) {
				this.deferredValue = object;
				this.updateCallback.accept(this.deferredValue);
			}
		}
	}

	public static DeferredSaveOption<Boolean> createBoolean(boolean defaultValue, String key) {
		return new DeferredSaveOption<>(
				"rainglow.config." + key,
				Option.constantTooltip(Text.translatable("rainglow.config.tooltip." + key)),
				(text, value) -> GameOptions.getGenericValueText(text, Text.translatable("ramel.config.value." + key, value)),
				Option.BOOLEAN_VALUES,
				defaultValue,
				value -> {
				}
		);
	}

	public void save() {
		this.value = this.deferredValue;
	}
}
