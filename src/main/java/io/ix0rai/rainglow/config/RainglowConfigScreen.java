package io.ix0rai.rainglow.config;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.option.SpruceBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceIntegerInputOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RainglowConfigScreen extends RainglowScreen {
    private final SpruceOption modeOption;
    private final SpruceOption customOption;
    private final SpruceOption[] entityToggles = new SpruceOption[RainglowEntity.values().length];
    private final SpruceOption resetOption;
    private final SpruceOption saveOption;

    private final SpruceOption colourRarityOption;
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
                        this.remove(this.coloursToApplyLabel);
                        this.coloursToApplyLabel = createColourListLabel(Rainglow.translatableTextKey("config.colours_to_apply"), this.mode, this.width / 2 - 125, this.height / 4 + 40);
                        this.addDrawable(this.coloursToApplyLabel);
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

        // toggles whether entities are rainbow
        for (int i = 0; i < RainglowEntity.values().length; i ++) {
            RainglowEntity entity = RainglowEntity.values()[i];

            this.entityToggles[i] = createEntityToggle(
                    entity,
                    () -> Rainglow.CONFIG.isEntityEnabled(entity),
                    enabled -> Rainglow.CONFIG.setEntityEnabled(entity, enabled)
            );
        }

        this.colourRarityOption = new SpruceIntegerInputOption(Rainglow.translatableTextKey("config.rarity"),
                Rainglow.CONFIG::getRarity,
                Rainglow.CONFIG::setRarity,
                Rainglow.translatableText("tooltip.rarity")
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
                    this.closeScreen();
                    Rainglow.CONFIG.setMode(this.mode);
                    Rainglow.CONFIG.save(true);
                }
        );
    }

    private SpruceBooleanOption createEntityToggle(RainglowEntity entity, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new SpruceBooleanOption(Rainglow.translatableTextKey("config." + "enable_" + entity.getId()),
                getter,
                setter,
                null
        );
    }

    @Override
    protected void init() {
        super.init();

        SpruceOptionListWidget optionList = new SpruceOptionListWidget(Position.of(0, 22), this.width, this.height - (35 + 22));
        for (int i = 0; i < RainglowEntity.values().length; i += 2) {
            SpruceOption secondToggle = null;
            if (i + 1 < RainglowEntity.values().length) {
                secondToggle = this.entityToggles[i + 1];
            }

            optionList.addOptionEntry(this.entityToggles[i], secondToggle);
        }

        optionList.addOptionEntry(this.modeOption, this.customOption);
        optionList.addSingleOptionEntry(this.colourRarityOption);
        this.addDrawable(optionList);

        // current colours label and colours to apply label
        SpruceLabelWidget currentColoursLabel = createColourListLabel(Rainglow.translatableTextKey("config.current_colours"), Rainglow.CONFIG.getMode(), this.width / 2 - 290, this.height / 4 + 40);
        this.addDrawable(currentColoursLabel);
        this.coloursToApplyLabel = createColourListLabel(Rainglow.translatableTextKey("config.colours_to_apply"), this.mode, this.width / 2 - 125, this.height / 4 + 40);
        this.addDrawable(this.coloursToApplyLabel);

        // reset and save buttons
        this.addDrawable(this.resetOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
        this.addDrawable(this.saveOption.createWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150));
    }

    private SpruceLabelWidget createColourListLabel(String translationKey, RainglowMode mode, int x, int y) {
        // creates a label and appends all the colours that will be applied in the given mode
        StringBuilder text = new StringBuilder(Language.getInstance().get(translationKey));
        int maxDisplayedColourCount = 16;

        for (int i = 0; i < mode.getColours().size(); i += 2) {
            RainglowColour colour = mode.getColours().get(i);

            if (i < maxDisplayedColourCount) {
                String colour1 = Language.getInstance().get(Rainglow.translatableTextKey("colour." + colour.getId()));
                String colour2 = "";
                if (i + 1 <= mode.getColours().size() - 1) {
                    colour2 = Language.getInstance().get(Rainglow.translatableTextKey("colour." + mode.getColours().get(i + 1).getId()));
                }

                boolean appendComma = i + 2 < mode.getColours().size();

                text.append("\n").append(colour1).append(colour2.equals("") ? "" : ", ").append(colour2).append(appendComma ? "," : "");
            } else  {
                text.append("\n... ").append(mode.getColours().size() - maxDisplayedColourCount).append(" ").append(Language.getInstance().get(Rainglow.translatableTextKey("config.more")));
            }
        }

        // set colour to the mode's text colour
        Style style = Style.EMPTY.withColor(mode.getText().getStyle().getColor());
        return new SpruceLabelWidget(Position.of(this, x + 110, y), Text.literal(text.toString()).setStyle(style), 200, true);
    }

    private static void sendConfigLockedToast() {
        Toast toast = new SystemToast(SystemToast.Type.PACK_LOAD_FAILURE, Rainglow.translatableText("config.server_locked_title"), Rainglow.translatableText("config.server_locked_description"));
        MinecraftClient.getInstance().getToastManager().add(toast);
    }
}
