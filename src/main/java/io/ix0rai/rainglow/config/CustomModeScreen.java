package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.Option;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.Text;

public class CustomModeScreen extends SaveableGameOptionsScreen {
	private static final Text TITLE = Rainglow.translatableText("config.custom");

	public CustomModeScreen(Screen parent) {
		super(parent, TITLE);
	}

	@Override
	protected boolean validate() {
		boolean hasColourSelected = false;
		for (DeferredSaveOption<?> option : this.options) {
			if ((boolean) option.deferredValue) {
				hasColourSelected = true;
				break;
			}
		}

		if (!hasColourSelected) {
			sendNoColoursToast();
		}
		return hasColourSelected;
	}

	@Override
	protected void save() {
		Rainglow.CONFIG.customColours.getRealValue().clear();
		super.save();
		Rainglow.CONFIG.save();
	}

	@Override
	protected void method_60325() {
		for (RainglowColour colour : RainglowColour.values()) {
			this.options.add(DeferredSaveOption.createDeferredBoolean(
					"colour." + colour.getId(),
					null,
					Rainglow.CONFIG.customColours.getRealValue().contains(colour.getId()),
					enabled -> {
						if (enabled) {
							Rainglow.CONFIG.customColours.getRealValue().add(colour.getId());
						}
					},
					enabled -> this.saveButton.active = true
			));
		}

		this.field_51824.addEntries(this.options.toArray(new Option<?>[0]));
	}

	private static void sendNoColoursToast() {
		Toast toast = new SystemToast(SystemToast.Id.PACK_LOAD_FAILURE, Rainglow.translatableText("config.no_custom_colours"), Rainglow.translatableText("config.no_custom_colours_description"));
		MinecraftClient.getInstance().getToastManager().add(toast);
	}
}
