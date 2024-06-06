package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.CyclingButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.list.ButtonListWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeConfigScreen extends GameOptionsScreen implements ScreenWithUnsavedWarning {
	private final ButtonWidget saveButton;
	private final Map<RainglowEntity, RainglowMode> updatedModes = new HashMap<>();
	private boolean isConfirming;

	private static final Text TITLE = Rainglow.translatableText("config.custom");

	public ModeConfigScreen(Screen parent) {
		super(parent, MinecraftClient.getInstance().options, TITLE);
		this.saveButton = ButtonWidget.builder(Rainglow.translatableText("config.save"), button -> this.save()).build();
		this.saveButton.active = false;
	}

	private ClickableWidget createEntityCyclingWidget(RainglowEntity entity) {
		Text text = Text.translatable("entity.minecraft." + entity.getId());

		return CyclingButtonWidget.builder(RainglowMode::getText)
				.values(RainglowMode.values())
				// todo this will crash if the world is null
				.initially(Rainglow.MODE_CONFIG.getMode(MinecraftClient.getInstance().world, entity))
				.tooltip(RainglowConfigScreen::createColourListLabel)
				.build(
						text,
						(cyclingButtonWidget, mode) -> {
							this.saveButton.active = true;
							this.updatedModes.put(entity, mode);
						}
				);
	}

	private void save() {
		for (Map.Entry<RainglowEntity, RainglowMode> entry : this.updatedModes.entrySet()) {
			Rainglow.MODE_CONFIG.setMode(MinecraftClient.getInstance().world, entry.getValue(), entry.getKey());
		}

		Rainglow.CONFIG.save();
		this.saveButton.active = false;
	}

	@Override
	public void init() {
		HeaderFooterLayoutWidget headerFooterWidget = new HeaderFooterLayoutWidget(this, 61, 33);
		headerFooterWidget.addToHeader(new TextWidget(TITLE, this.textRenderer), settings -> settings.alignHorizontallyCenter().setBottomPadding(28));

		if (!this.isConfirming) {
			ButtonListWidget buttonListWidget = headerFooterWidget.addToContents(new ButtonListWidget(this.client, this.width, this.height, this));
			for (RainglowEntity entity : RainglowEntity.values()) {
				buttonListWidget.method_58227(List.of(this.createEntityCyclingWidget(entity)));
			}

			LinearLayoutWidget linearLayout = headerFooterWidget.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
			linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
			linearLayout.add(this.saveButton);
		} else {
			this.setUpUnsavedWarning(headerFooterWidget, this.textRenderer, this.parent);
		}

		headerFooterWidget.visitWidgets(this::addDrawableSelectableElement);
		headerFooterWidget.arrangeElements();
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

