package io.ix0rai.rainglow.config;

import net.minecraft.client.option.Option;

public class DeferredSaveOption<T> extends Option<T> {
	private final T deferredValue;
}
