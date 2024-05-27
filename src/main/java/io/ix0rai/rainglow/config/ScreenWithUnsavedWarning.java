package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.CommonTexts;

public interface ScreenWithUnsavedWarning {
	void setConfirming(boolean confirming);

	void clearAndInit();

	default void  setUpUnsavedWarning(HeaderFooterLayoutWidget headerFooterWidget, TextRenderer renderer, Screen parent) {
		LinearLayoutWidget contentWidget = headerFooterWidget.addToContents(new LinearLayoutWidget(250, 100, LinearLayoutWidget.Orientation.VERTICAL).setSpacing(8));
		contentWidget.add(new TextWidget(Rainglow.translatableText("config.unsaved_warning"), renderer), LayoutSettings::alignHorizontallyCenter);

		LinearLayoutWidget buttons = new LinearLayoutWidget(250, 20, LinearLayoutWidget.Orientation.HORIZONTAL).setSpacing(8);
		buttons.add(ButtonWidget.builder(Rainglow.translatableText("config.continue_editing"), (buttonWidget) -> {
			this.setConfirming(false);
			this.clearAndInit();
		}).build());
		buttons.add(ButtonWidget.builder(CommonTexts.YES, (buttonWidget) -> MinecraftClient.getInstance().setScreen(parent)).build());

		contentWidget.add(buttons, LayoutSettings::alignHorizontallyCenter);
	}
}
