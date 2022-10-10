package io.ix0rai.rainglow.config;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RainglowConfigScreen extends SpruceScreen {
    private final Screen parent;

    private final SpruceOption modeOption;
    private final SpruceOption customOption;
    private final SpruceOption resetOption;
    private RainglowMode mode;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(Rainglow.translatableText("config.title"));
        this.parent = parent;
        this.mode = Rainglow.CONFIG.getMode();

        this.modeOption = new SpruceCyclingOption(Rainglow.translatableTextKey("config.mode"),
                amount -> mode = mode.cycle(),
                option -> option.getDisplayText(mode.getText()),
                Rainglow.translatableText("tooltip.mode",
                        List.of(RainglowMode.values())
                )
        );

        this.customOption = new SpruceSimpleActionOption(Rainglow.translatableTextKey("config.custom"),
                (position, width, message, action) -> new SpruceButtonWidget(position, width, 20, message, action),
                btn -> MinecraftClient.getInstance().setScreen(new CustomModeScreen(this)),
                null
        );

        this.resetOption = SpruceSimpleActionOption.reset(btn -> {
            this.mode = RainglowMode.RAINBOW;
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
        int buttonOffset = 30;

        this.addDrawableChild(this.modeOption.createWidget(Position.of(this.width / 2 - 205, this.height / 6 - buttonHeight + buttonOffset), 200));
        this.addDrawableChild(this.customOption.createWidget(Position.of(this.width / 2 + 5, this.height / 6 - buttonHeight + buttonOffset), 200));

        this.addDrawableChild(new SpruceLabelWidget(Position.of(this, 0, this.height / 9), Rainglow.translatableText("config.title"), this.width, true));
        StringBuilder text = new StringBuilder(Language.getInstance().get(Rainglow.translatableTextKey("config.current_colours")));
        for (SquidColour colour : Rainglow.CONFIG.getMode().getColours()) {
            text.append("\n").append(Language.getInstance().get(Rainglow.translatableTextKey("colour." + colour.getId())));
        }
        this.addDrawableChild(new SpruceLabelWidget(Position.of(this, 0, this.height / 4 + buttonHeight), Text.of(text.toString()).copy().setStyle(Style.EMPTY.withColor(Rainglow.CONFIG.getMode().getText().getStyle().getColor())), this.width, true));

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
