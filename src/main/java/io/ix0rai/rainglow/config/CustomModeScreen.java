package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.list.ButtonListWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.option.Option;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CustomModeScreen extends GameOptionsScreen {
	private final ButtonWidget saveButton;
	private final List<DeferredSaveOption<Boolean>> options = new ArrayList<>();

	private static final Text TITLE = Rainglow.translatableText("config.custom");

	public CustomModeScreen(Screen parent) {
		super(parent, MinecraftClient.getInstance().options, TITLE);
		this.saveButton = ButtonWidget.builder(Rainglow.translatableText("config.save"), button -> {
			boolean hasColourSelected = false;
			for (DeferredSaveOption<Boolean> option : this.options) {
				if (option.deferredValue) {
					hasColourSelected = true;
					break;
				}
			}

			if (!hasColourSelected) {
				sendNoColoursToast();
			} else {
				this.save();
				this.closeScreen();
			}
		}).build();
		this.saveButton.active = false;
	}

	private void createColourToggles() {
		this.options.clear();

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
	}

	private void save() {
		Rainglow.CONFIG.customColours.getRealValue().clear();

		for (DeferredSaveOption<?> option : this.options) {
			option.save();
		}

		Rainglow.CONFIG.save();
	}

	@Override
	public void init() {
		HeaderFooterLayoutWidget headerFooterWidget = new HeaderFooterLayoutWidget(this, 61, 33);
		headerFooterWidget.addToHeader(new TextWidget(TITLE, this.textRenderer), settings -> settings.alignHorizontallyCenter().setBottomPadding(28));

		ButtonListWidget buttonListWidget = headerFooterWidget.addToContents(new ButtonListWidget(this.client, this.width, this.height, this));
		createColourToggles();
		buttonListWidget.addEntries(this.options.toArray(new Option<?>[0]));

		LinearLayoutWidget linearLayout = headerFooterWidget.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
		linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
		linearLayout.add(this.saveButton);

		headerFooterWidget.visitWidgets(this::addDrawableSelectableElement);
		headerFooterWidget.arrangeElements();
	}

	private static void sendNoColoursToast() {
		Toast toast = new SystemToast(SystemToast.Id.PACK_LOAD_FAILURE, Rainglow.translatableText("config.no_custom_colours"), Rainglow.translatableText("config.no_custom_colours_description"));
		MinecraftClient.getInstance().getToastManager().add(toast);
	}
}
