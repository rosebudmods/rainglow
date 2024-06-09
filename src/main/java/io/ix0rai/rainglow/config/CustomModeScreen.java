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

public class CustomModeScreen extends GameOptionsScreen implements ScreenWithUnsavedWarning {
	private final ButtonWidget saveButton;
	private final List<DeferredSaveOption<Boolean>> options = new ArrayList<>();
	private boolean isConfirming;

	private static final Text TITLE = Rainglow.translatableText("config.custom");

	public CustomModeScreen(Screen parent) {
		super(parent, MinecraftClient.getInstance().options, TITLE);

		this.saveButton = ButtonWidget.builder(
				Rainglow.translatableText("config.save"),
				button -> {
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
		this.saveButton.active = false;
	}

	@Override
	public void init() {
		HeaderFooterLayoutWidget headerFooterWidget = new HeaderFooterLayoutWidget(this, 61, 33);
		headerFooterWidget.addToHeader(new TextWidget(TITLE, this.textRenderer), settings -> settings.alignHorizontallyCenter().setBottomPadding(28));

		if (!this.isConfirming) {
			ButtonListWidget buttonListWidget = headerFooterWidget.addToContents(new ButtonListWidget(this.client, this.width, this));
			createColourToggles();
			buttonListWidget.addEntries(this.options.toArray(new Option<?>[0]));

			LinearLayoutWidget linearLayout = headerFooterWidget.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
			linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
			linearLayout.add(this.saveButton);
		} else {
			this.setUpUnsavedWarning(headerFooterWidget, this.textRenderer, this.parent);
		}

		headerFooterWidget.visitWidgets(this::addDrawableSelectableElement);
		headerFooterWidget.arrangeElements();
	}

	@Override
	protected void method_60325() {}

	@Override
	protected void repositionElements() {
		this.clearAndInit();
	}

	private static void sendNoColoursToast() {
		Toast toast = new SystemToast(SystemToast.Id.PACK_LOAD_FAILURE, Rainglow.translatableText("config.no_custom_colours"), Rainglow.translatableText("config.no_custom_colours_description"));
		MinecraftClient.getInstance().getToastManager().add(toast);
	}

	@Override
	public void setConfirming(boolean confirming) {
		this.isConfirming = confirming;
	}

	@Override
	public void clearAndInit() {
		super.clearAndInit();
	}

	@Override
	public void closeScreen() {
		if (this.saveButton.active) {
			this.isConfirming = true;
			this.clearAndInit();
		} else {
			MinecraftClient.getInstance().setScreen(this.parent);
		}
	}
}
