package io.ix0rai.rainglow.config;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.data.SquidColour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RainglowConfigScreen extends RainglowScreen {
    private final SpruceOption modeOption;
    private final SpruceOption customOption;
    private final SpruceOption resetOption;
    private final SpruceOption saveOption;
    private RainglowMode mode;
    // colours to apply is saved in a variable so that it can be removed from the screen when cycling modes
    private SpruceLabelWidget coloursToApplyLabel;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(parent, Rainglow.translatableText("config.title"));
        this.mode = Rainglow.CONFIG.getMode();

        // mode option cycles through available modes
        // it also updates the label to show which colours will be applied
        this.modeOption = new SpruceCyclingOption(Rainglow.translatableTextKey("config.mode"),
                amount -> {
                    if (!Rainglow.CONFIG.isEditLocked(MinecraftClient.getInstance())) {
                        mode = mode.cycle();
                        this.remove(coloursToApplyLabel);
                        this.coloursToApplyLabel = createColourListLabel(Rainglow.translatableTextKey("config.colours_to_apply"), this.mode, this.width / 2 - 108, this.height / 4 + 20);
                        this.addDrawableChild(coloursToApplyLabel);
                    } else {
                        sendConfigLockedToast();
                    }
                },
                option -> option.getDisplayText(mode.getText()),
                Rainglow.translatableText("tooltip.mode",
                        List.of(RainglowMode.values())
                )
        );

        // opens a screen to toggle which colours are applied in custom mode
        this.customOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.custom"),
                btn -> MinecraftClient.getInstance().setScreen(new CustomModeScreen(this))
        );

        // resets the config to default values
        this.resetOption = SpruceSimpleActionOption.reset(btn -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!Rainglow.CONFIG.isEditLocked(client)) {
                this.mode = RainglowMode.getDefault();
                this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
            } else {
                sendConfigLockedToast();
            }
        });

        // saves values to config file
        this.saveOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.save"),
                buttonWidget -> {
                    this.onClose();
                    Rainglow.CONFIG.setMode(this.mode, true);
                }
        );
    }

    @Override
    protected void init() {
        super.init();

        // config title
        this.addDrawableChild(new SpruceLabelWidget(Position.of(this, 0, this.height / 9), Rainglow.translatableText("config.title"), this.width, true));

        int buttonHeight = 20;
        int buttonOffset = 30;

        // mode cycling option and custom mode screen button
        this.addDrawableChild(this.modeOption.createWidget(Position.of(this.width / 2 - 205, this.height / 6 - buttonHeight + buttonOffset), 200));
        this.addDrawableChild(this.customOption.createWidget(Position.of(this.width / 2 + 5, this.height / 6 - buttonHeight + buttonOffset), 200));

        // current colours label and colours to apply label
        SpruceLabelWidget currentColoursLabel = createColourListLabel(Rainglow.translatableTextKey("config.current_colours"), Rainglow.CONFIG.getMode(), this.width / 2 - 318, this.height / 4 + buttonHeight);
        this.addDrawableChild(currentColoursLabel);
        this.coloursToApplyLabel = createColourListLabel(Rainglow.translatableTextKey("config.colours_to_apply"), this.mode, this.width / 2 - 108, this.height / 4 + buttonHeight);
        this.addDrawableChild(coloursToApplyLabel);

        // reset and save buttons
        this.addDrawableChild(this.resetOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
        this.addDrawableChild(this.saveOption.createWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150));
    }

    private SpruceLabelWidget createColourListLabel(String translationKey, RainglowMode mode, int x, int y) {
        // creates a label and appends all the colours that will be applied in the given mode
        StringBuilder text = new StringBuilder(Language.getInstance().get(translationKey));
        for (SquidColour colour : mode.getColours()) {
            text.append("\n").append(Language.getInstance().get(Rainglow.translatableTextKey("colour." + colour.getId())));
        }
        // set colour to the mode's text colour
        Style style = Style.EMPTY.withColor(mode.getText().getStyle().getColor());
        return new SpruceLabelWidget(Position.of(this, x + 110, y), new LiteralText(text.toString()).setStyle(style), 200, true);
    }

    private static void sendConfigLockedToast() {
        Toast toast = new SystemToast(SystemToast.Type.PACK_LOAD_FAILURE, Rainglow.translatableText("config.server_locked_title"), Rainglow.translatableText("config.server_locked_description"));
        MinecraftClient.getInstance().getToastManager().add(toast);
    }
}
