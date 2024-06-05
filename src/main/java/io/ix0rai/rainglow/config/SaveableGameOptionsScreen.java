package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class SaveableGameOptionsScreen extends GameOptionsScreen {
    protected final ButtonWidget saveButton;
    protected final List<DeferredSaveOption<?>> options = new ArrayList<>();

    public SaveableGameOptionsScreen(Screen parent, Text title) {
        super(parent, MinecraftClient.getInstance().options, title);

        this.saveButton = ButtonWidget.builder(
            Rainglow.translatableText("config.save"),
            button -> {
                if (this.validate()) {
                    this.save();
                }
            }).build();
        this.saveButton.active = false;
    }

    protected boolean validate() {
        return true;
    }

    protected void save() {
        for (DeferredSaveOption<?> option : this.options) {
            option.save();
        }

        this.saveButton.active = false;
    }

    @Override
    protected void method_31387() {
        LinearLayoutWidget linearLayout = this.field_49503.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
        linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
        linearLayout.add(this.saveButton);
    }

    @Override
    public void closeScreen() {
        if (this.saveButton.active) {
            MinecraftClient.getInstance().setScreen(new ConfirmScreen(this.title));
        } else {
            MinecraftClient.getInstance().setScreen(this.parent);
        }
    }

    public class ConfirmScreen extends Screen {
        protected ConfirmScreen(Text title) {
            super(title);
        }

        @Override
        protected void init() {
            HeaderFooterLayoutWidget headerFooterWidget = new HeaderFooterLayoutWidget(this, 61, 33);
            headerFooterWidget.addToHeader(new TextWidget(this.title, this.textRenderer), settings -> settings.alignHorizontallyCenter().setBottomPadding(28));

            LinearLayoutWidget contentWidget = headerFooterWidget.addToContents(new LinearLayoutWidget(250, 100, LinearLayoutWidget.Orientation.VERTICAL).setSpacing(8));
            contentWidget.add(new TextWidget(Rainglow.translatableText("config.unsaved_warning"), this.textRenderer), LayoutSettings::alignHorizontallyCenter);

            LinearLayoutWidget buttons = new LinearLayoutWidget(250, 20, LinearLayoutWidget.Orientation.HORIZONTAL).setSpacing(8);
            buttons.add(ButtonWidget.builder(Rainglow.translatableText("config.continue_editing"), (buttonWidget) -> {
                MinecraftClient.getInstance().setScreen(SaveableGameOptionsScreen.this);
            }).build());
            buttons.add(ButtonWidget.builder(CommonTexts.YES, (buttonWidget) -> MinecraftClient.getInstance().setScreen(parent)).build());

            contentWidget.add(buttons, LayoutSettings::alignHorizontallyCenter);

            headerFooterWidget.visitWidgets(this::addDrawableSelectableElement);
            headerFooterWidget.arrangeElements();
        }
    }
}
