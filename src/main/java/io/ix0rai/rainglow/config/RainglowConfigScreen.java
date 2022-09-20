package io.ix0rai.rainglow.config;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import io.ix0rai.rainglow.Rainglow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class RainglowConfigScreen extends SpruceScreen {
    private final Screen parent;

    private final SpruceOption modeOption;
    private final SpruceOption resetOption;
    private RainglowMode mode;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(Rainglow.translatableText("config.title"));
        this.parent = parent;
        this.mode = Rainglow.CONFIG.getMode();

        this.modeOption = new SpruceCyclingOption(Rainglow.translatableTextKey("config.mode"),
                amount -> mode = mode.next(),
                option -> option.getDisplayText(mode.getTranslatedText()),
                Text.translatable(Rainglow.MOD_ID + ".tooltip.mode",
                        RainglowMode.DEFAULT.getTranslatedText(),
                        RainglowMode.ONLY_RED.getTranslatedText()
                )
        );

        this.resetOption = SpruceSimpleActionOption.reset(btn -> {
            this.mode = RainglowMode.DEFAULT;
            MinecraftClient client = MinecraftClient.getInstance();
            this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });
    }

    @Override
    public void closeScreen() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        } else {
            super.closeScreen();
        }
    }

    @Override
    protected void init() {
        super.init();

        int buttonHeight = 20;

        SpruceOptionListWidget options = new SpruceOptionListWidget(Position.of(0, 22), this.width, this.height - (35 + 22));
        options.addOptionEntry(this.modeOption, null);
        this.addDrawableChild(options);

        this.addDrawableChild(this.resetOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
        this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150,
                buttonHeight, Rainglow.translatableText("config.save"),
                buttonWidget -> {
                    this.closeScreen();
                    Rainglow.CONFIG.setMode(this.mode);
                }
        ));
    }
}
