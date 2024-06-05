package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.CyclingButtonWidget;
import net.minecraft.client.gui.widget.layout.GridWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

public class RainglowConfigScreen extends SaveableGameOptionsScreen {
    private static final Text TITLE = Rainglow.translatableText("config.title");
    public static final Text YES = Text.translatable("gui.yes").styled(style -> style.withColor(0x00FF00));
    public static final Text NO = Text.translatable("gui.no").styled(style -> style.withColor(0xFF0000));

    private RainglowMode mode;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(parent, TITLE);

        this.mode = getMode();
    }

    private void setMode(RainglowMode mode) {
        if (MinecraftClient.getInstance().world == null) {
            Rainglow.CONFIG.defaultMode.setValue(mode.getId());
        } else {
            Rainglow.MODE_CONFIG.setMode(MinecraftClient.getInstance().world, mode);
        }
    }

    private RainglowMode getMode() {
        if (MinecraftClient.getInstance().world == null) {
            return RainglowMode.get(Rainglow.CONFIG.defaultMode.getRealValue());
        } else {
            return Rainglow.MODE_CONFIG.getMode(MinecraftClient.getInstance().world);
        }
    }

    private TextWidget getInfoText() {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            return new TextWidget((Rainglow.RAINGLOW_DATAPACKS.size() == 1 ? Rainglow.translatableText("config.loaded_builtin", RainglowMode.values().size()) : Rainglow.translatableText("config.loaded_datapacks", RainglowMode.values().size(), Rainglow.RAINGLOW_DATAPACKS.size())), this.textRenderer).setTextColor(0x00FFFF);
        } else if (MinecraftClient.getInstance().world != null) {
            return new TextWidget(Rainglow.translatableText("config.server_locked"), this.textRenderer).setTextColor(0xFF0000);
        } else {
            return new TextWidget(Rainglow.translatableText("config.no_world"), this.textRenderer).setTextColor(0xFF0000);
        }
    }

    @Override
    protected void save() {
        super.save();
        this.setMode(this.mode);
    }

    @Override
    protected void method_60325() {
        LinearLayoutWidget contentLayout = LinearLayoutWidget.createVertical().setSpacing(8);

        contentLayout.add(createModeButton(), LayoutSettings::alignVerticallyBottom);
        contentLayout.add(getInfoText(), LayoutSettings::alignHorizontallyCenter);

        GridWidget gridWidget = new GridWidget();
        gridWidget.getDefaultSettings().setHorizontalPadding(4).setBottomPadding(4).alignHorizontallyCenter();

        GridWidget.AdditionHelper mainAdditionHelper = gridWidget.createAdditionHelper(2);
        for (RainglowEntity entity : RainglowEntity.values()) {
            DeferredSaveOption<Boolean> entityToggle = createEntityToggle(entity);
            mainAdditionHelper.add(entityToggle.createButton(MinecraftClient.getInstance().options));
            entityToggle.set(entityToggle.deferredValue);
            this.options.add(entityToggle);

            DeferredSaveOption<Integer> raritySlider = createColourRaritySlider(entity);
            mainAdditionHelper.add(raritySlider.createButton(MinecraftClient.getInstance().options));
            this.options.add(raritySlider);
        }

        contentLayout.add(gridWidget);
        contentLayout.add(ButtonWidget.builder(
                        Rainglow.translatableText("config.custom"),
                        button -> MinecraftClient.getInstance().setScreen(new CustomModeScreen(this))
                ).width(308).position(4, 0).build(),
                LayoutSettings.create().setPadding(4, 0));

        this.field_49503.addToContents(contentLayout);
    }

    private DeferredSaveOption<Boolean> createEntityToggle(RainglowEntity entity) {
        return DeferredSaveOption.createDeferredBoolean(
                "config.enable_" + entity.getId(),
                "tooltip.entity_toggle",
                Rainglow.CONFIG.toggles.getRealValue().get(entity.getId()),
                enabled -> Rainglow.CONFIG.toggles.getRealValue().put(entity.getId(), enabled),
                enabled -> this.saveButton.active = true
        );
    }

    private DeferredSaveOption<Integer> createColourRaritySlider(RainglowEntity entity) {
        return DeferredSaveOption.createDeferredRangedInt(
                "config." + entity.getId() + "_rarity",
                "tooltip.rarity",
                Rainglow.CONFIG.rarities.getRealValue().get(entity.getId()),
                0,
                100,
                rarity -> Rainglow.CONFIG.rarities.getRealValue().put(entity.getId(), rarity),
                rarity -> this.saveButton.active = true
        );
    }

    public CyclingButtonWidget<RainglowMode> createModeButton() {
        return CyclingButtonWidget.builder(RainglowMode::getText)
                .values(RainglowMode.values())
                .initially(this.mode)
                .tooltip(this::createColourListLabel)
                .build(
                        0,
                        0,
                        308,
                        20,
                        Rainglow.translatableText("config.mode"),
                        (cyclingButtonWidget, mode) -> {
                            this.saveButton.active = true;
                            this.mode = mode;
                        }
                );
    }

    private Tooltip createColourListLabel(RainglowMode mode) {
        // creates a label and appends all the colours that will be applied in the given mode
        StringBuilder text = new StringBuilder(Language.getInstance().get(Rainglow.translatableTextKey("config.colours_to_apply")));
        int maxDisplayedColourCount = 16;
        int maxColoursPerLine = 4;

        for (int i = 0; i < mode.getColours().size(); i += maxColoursPerLine) {
            if (i < maxDisplayedColourCount) {
                text.append("\n");

                int coloursLeft = mode.getColours().size() - i;
                int coloursToDisplay = Math.min(coloursLeft, maxColoursPerLine);

                for (int j = 0; j < coloursToDisplay; j++) {
                    RainglowColour currentColour = mode.getColours().get(i + j);
                    text.append(Language.getInstance().get(Rainglow.translatableTextKey("colour." + currentColour.getId())));
                    if (j < coloursToDisplay - 1) {
                        text.append(", ");
                    }
                }
            } else  {
                text.append("\n... ").append(mode.getColours().size() - maxDisplayedColourCount).append(" ").append(Language.getInstance().get(Rainglow.translatableTextKey("config.more")));
            }
        }

        // set colour to the mode's text colour
        Style style = Style.EMPTY.withColor(mode.getText().getStyle().getColor());
        return Tooltip.create(Text.literal(text.toString()).setStyle(style));
    }
}
