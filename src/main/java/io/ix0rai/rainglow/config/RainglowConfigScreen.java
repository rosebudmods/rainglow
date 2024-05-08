package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.SimpleOptionsScreen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.option.Option;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RainglowConfigScreen extends SimpleOptionsScreen {
//    private final SpruceOption modeOption;
//    private final SpruceOption customOption;
//    private final SpruceOption resetOption;
//    private final SpruceOption saveOption;

    //private final SpruceOption colourRarityOption;
    //private RainglowMode mode;
    // colours to apply is saved in a variable so that it can be removed from the screen when cycling modes
   // private SpruceLabelWidget coloursToApplyLabel;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Rainglow.translatableText("config.title"),
                new Option[]{
                    createEntityToggles().get(0),
                    createColourRaritySliders().get(0),
                    createEntityToggles().get(1),
                    createColourRaritySliders().get(1),
                    createEntityToggles().get(2),
                    createColourRaritySliders().get(2),
                }
        );



//        // mode option cycles through available modes
//        // it also updates the label to show which colours will be applied
//        this.modeOption = new SpruceCyclingOption(Rainglow.translatableTextKey("config.mode"),
//                amount -> {
//                    if (!Rainglow.CONFIG.isEditLocked(MinecraftClient.getInstance())) {
//                        mode = mode.cycle();
//                        this.remove(this.coloursToApplyLabel);
//                        this.coloursToApplyLabel = createColourListLabel(Rainglow.translatableTextKey("config.colours_to_apply"), this.mode, this.width / 2 - 125, this.height / 4 + 40);
//                        this.addDrawable(this.coloursToApplyLabel);
//                    } else {
//                        sendConfigLockedToast();
//                    }
//                },
//                option -> option.getDisplayText(mode.getText()),
//                Rainglow.translatableText("tooltip.mode",
//                        List.of(RainglowMode.values())
//                )
//        );
//
//        // opens a screen to toggle which colours are applied in custom mode
//        this.customOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.custom"),
//                btn -> MinecraftClient.getInstance().setScreen(new CustomModeScreen(this))
//        );
//
//        // toggles whether entities are rainbow
//        for (int i = 0; i < RainglowEntity.values().length; i ++) {
//            RainglowEntity entity = RainglowEntity.values()[i];
//
//            this.entityToggles[i] = createEntityToggle(
//                    entity,
//                    () -> Rainglow.CONFIG.isEntityEnabled(entity),
//                    enabled -> Rainglow.CONFIG.setEntityEnabled(entity, enabled)
//            );
//        }
//
//        this.colourRarityOption = new SpruceIntegerInputOption(Rainglow.translatableTextKey("config.rarity"),
//                Rainglow.CONFIG::getRarity,
//                Rainglow.CONFIG::setRarity,
//                Rainglow.translatableText("tooltip.rarity")
//        );
//
//        // resets the config to default values
//        this.resetOption = SpruceSimpleActionOption.reset(btn -> {
//            MinecraftClient client = MinecraftClient.getInstance();
//            if (!Rainglow.CONFIG.isEditLocked(client)) {
//                this.mode = RainglowMode.getDefault();
//                this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
//            } else {
//                sendConfigLockedToast();
//            }
//        });
//
//        // saves values to config file
//        this.saveOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.save"),
//                buttonWidget -> {
//                    this.closeScreen();
//                    Rainglow.CONFIG.setMode(this.mode);
//                    Rainglow.CONFIG.save(true);
//                }
//        );
    }

    private static List<Option<Boolean>> createEntityToggles() {
        List<Option<Boolean>> toggles = new ArrayList<>();

        for (RainglowEntity entity : RainglowEntity.values()) {
            toggles.add(DeferredSaveOption.createDeferredBoolean(
                "enable_" + entity.getId(),
                Rainglow.CONFIG.isEntityEnabled(entity),
                enabled -> Rainglow.CONFIG.setEntityEnabled(entity, enabled)
            ));
        }

        return toggles;
    }

    private static List<Option<Integer>> createColourRaritySliders() {
        List<Option<Integer>> sliders = new ArrayList<>();

        for (RainglowEntity entity : RainglowEntity.values()) {
            sliders.add(DeferredSaveOption.createDeferredRangedInt(
                entity.getId() + "_rarity",
                Rainglow.CONFIG.getRarity(entity),
                0,
                100,
                rarity -> Rainglow.CONFIG.setRarity(entity, rarity)
            ));
        }

        return sliders;
    }

    private void save() {
        for (Option<?> option : this.options) {
            if (option instanceof DeferredSaveOption) {
                ((DeferredSaveOption<?>) option).save();
            }
        }
    }

    @Override
    protected void method_31387() {
        LinearLayoutWidget linearLayout = this.field_49503.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
        linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
        linearLayout.add(ButtonWidget.builder(CommonTexts.YES, button -> {
            this.save();
            this.closeScreen();
        }).build());
        this.field_49503.visitWidgets(this::addDrawableSelectableElement);
        this.repositionElements();
    }

    private TextWidget createColourListLabel(String translationKey, RainglowMode mode, int x, int y) {
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

                text.append("\n").append(colour1).append(colour2.isEmpty() ? "" : ", ").append(colour2).append(appendComma ? "," : "");
            } else  {
                text.append("\n... ").append(mode.getColours().size() - maxDisplayedColourCount).append(" ").append(Language.getInstance().get(Rainglow.translatableTextKey("config.more")));
            }
        }

        // set colour to the mode's text colour
        Style style = Style.EMPTY.withColor(mode.getText().getStyle().getColor());
        return new TextWidget(Text.literal(text.toString()).setStyle(style), MinecraftClient.getInstance().textRenderer);
    }

    private static void sendConfigLockedToast() {
        Toast toast = new SystemToast(SystemToast.Id.PACK_LOAD_FAILURE, Rainglow.translatableText("config.server_locked_title"), Rainglow.translatableText("config.server_locked_description"));
        MinecraftClient.getInstance().getToastManager().add(toast);
    }
}
