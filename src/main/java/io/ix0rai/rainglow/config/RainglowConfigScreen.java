package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.button.CyclingButtonWidget;
import net.minecraft.client.gui.widget.layout.GridWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.option.Option;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RainglowConfigScreen extends Screen implements ScreenWithUnsavedWarning {
    private static final Text TITLE = Rainglow.translatableText("config.title");
    public static final Text YES = Text.translatable("gui.yes").styled(style -> style.withColor(0x00FF00));
    public static final Text NO = Text.translatable("gui.no").styled(style -> style.withColor(0xFF0000));

    private final Screen parent;
    private final Map<RainglowEntity, DeferredSaveOption<Boolean>> toggles = new HashMap<>();
    private final Map<RainglowEntity, DeferredSaveOption<Integer>> sliders = new HashMap<>();
    private final ButtonWidget saveButton;

    private RainglowMode mode;
    private boolean isConfirming;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
        if (MinecraftClient.getInstance().world != null) {
            var modes = Rainglow.MODE_CONFIG.getModes(MinecraftClient.getInstance().world);
            // if all modes are the same, set the mode to that mode, otherwise set it to null
            this.mode = modes.entrySet().stream().allMatch(entry -> entry.getValue().equals(modes.get(RainglowEntity.GLOW_SQUID))) ? modes.get(RainglowEntity.GLOW_SQUID) : null;
        } else {
            this.mode = RainglowMode.get(Rainglow.CONFIG.defaultMode.getRealValue());
        }

        this.saveButton = ButtonWidget.builder(Rainglow.translatableText("config.save"), button -> this.save()).build();
        this.saveButton.active = false;
    }

    private void setMode(RainglowMode mode) {
        if (MinecraftClient.getInstance().world == null) {
            Rainglow.CONFIG.defaultMode.setValue(mode.getId());
        } else {
            Rainglow.MODE_CONFIG.setMode(MinecraftClient.getInstance().world, mode, null);
        }
    }

    private RainglowMode getMode(RainglowEntity entity) {
        if (MinecraftClient.getInstance().world == null) {
            return RainglowMode.get(Rainglow.CONFIG.defaultMode.getRealValue());
        } else {
            return Rainglow.MODE_CONFIG.getMode(MinecraftClient.getInstance().world, entity);
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
    public void init() {
        HeaderFooterLayoutWidget headerFooterWidget = new HeaderFooterLayoutWidget(this, 61, 33);
        LinearLayoutWidget headerLayout = headerFooterWidget.addToHeader(LinearLayoutWidget.createVertical().setSpacing(8));

        if (!this.isConfirming) {
            // header
            headerLayout.add(new TextWidget(TITLE, this.textRenderer), settings -> settings.alignHorizontallyCenter().alignVerticallyTop().setPadding(12));
            LinearLayoutWidget modeLayout = headerLayout.add(LinearLayoutWidget.createHorizontal().setSpacing(8), LayoutSettings::alignVerticallyBottom);
            modeLayout.add(createModeButton(), LayoutSettings::alignVerticallyBottom);
            // todo link to page with per-entity mode editing
            modeLayout.add(ButtonWidget.builder(Text.literal("grind"), button -> {
                this.client.setScreen(new ModeConfigScreen(this));
            }).width(100).build(), LayoutSettings::alignVerticallyBottom);
            headerLayout.add(getInfoText(), settings -> settings.alignHorizontallyCenter().alignVerticallyBottom().setBottomPadding(1));

            // contents
            LinearLayoutWidget contentLayout = LinearLayoutWidget.createVertical();

            GridWidget gridWidget = new GridWidget();
            gridWidget.getDefaultSettings().setHorizontalPadding(4).setBottomPadding(4).alignHorizontallyCenter();

            GridWidget.AdditionHelper mainAdditionHelper = gridWidget.createAdditionHelper(2);
            for (RainglowEntity entity : RainglowEntity.values()) {
                DeferredSaveOption<Boolean> entityToggle = createEntityToggle(entity);
                mainAdditionHelper.add(entityToggle.createButton(MinecraftClient.getInstance().options));
                entityToggle.set(entityToggle.deferredValue);

                mainAdditionHelper.add(createColourRaritySlider(entity).createButton(MinecraftClient.getInstance().options));
            }

            contentLayout.add(gridWidget);
            contentLayout.add(ButtonWidget.builder(Rainglow.translatableText("config.custom"), button -> MinecraftClient.getInstance().setScreen(new CustomModeScreen(this))).width(308).position(4, 0).build(), LayoutSettings.create().setPadding(4, 0));

            headerFooterWidget.addToContents(contentLayout);

            // footer
            LinearLayoutWidget linearLayout = headerFooterWidget.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));
            linearLayout.add(ButtonWidget.builder(CommonTexts.DONE, button -> this.closeScreen()).build());
            linearLayout.add(this.saveButton);
        } else {
            this.setUpUnsavedWarning(headerFooterWidget, this.textRenderer, this.parent);
        }

        headerFooterWidget.visitWidgets(this::addDrawableSelectableElement);
        headerFooterWidget.arrangeElements();
    }

    private DeferredSaveOption<Boolean> createEntityToggle(RainglowEntity entity) {
        return toggles.computeIfAbsent(entity, e -> DeferredSaveOption.createDeferredBoolean(
                "config.enable_" + e.getId(),
                "tooltip.entity_toggle",
                Rainglow.CONFIG.toggles.getRealValue().get(e.getId()),
                enabled -> Rainglow.CONFIG.toggles.getRealValue().put(e.getId(), enabled),
                enabled -> this.saveButton.active = true
        ));
    }

    private DeferredSaveOption<Integer> createColourRaritySlider(RainglowEntity entity) {
        return sliders.computeIfAbsent(entity, e -> DeferredSaveOption.createDeferredRangedInt(
				"config." + e.getId() + "_rarity",
				"tooltip.rarity",
				Rainglow.CONFIG.rarities.getRealValue().get(e.getId()),
				0,
				100,
				rarity -> Rainglow.CONFIG.rarities.getRealValue().put(e.getId(), rarity),
                rarity -> this.saveButton.active = true
		));
    }

    public ClickableWidget createModeButton() {
        if (mode != null) {
            return CyclingButtonWidget.builder(RainglowMode::getText)
                    .values(RainglowMode.values())
                    .initially(this.mode)
                    .tooltip(RainglowConfigScreen::createColourListLabel)
                    .build(
                            0,
                            0,
                            200,
                            20,
                            Rainglow.translatableText("config.mode"),
                            (cyclingButtonWidget, mode) -> {
                                this.saveButton.active = true;
                                this.mode = mode;
                            }
                    );
        } else {
            return ButtonWidget.builder(Text.literal("Mode: Mixed"), button -> {
                this.mode = RainglowMode.get(Rainglow.CONFIG.defaultMode.getRealValue());
                this.saveButton.active = true;
                this.clearAndInit();
            }).tooltip(createTooltip()).width(200).build();
        }
    }

    private Tooltip createTooltip() {
        MutableText text = Text.empty().append("Modes:");
        for (RainglowEntity entity : RainglowEntity.values()) {
            text.append("\n").append(Text.translatable("entity.minecraft." + entity.getId())).append(": ").append(this.getMode(entity).getText());
        }

        return Tooltip.create(text);
    }

    private void save() {
        Collection<Option<?>> options = new ArrayList<>(this.sliders.values());
        options.addAll(this.toggles.values());

        for (Option<?> option : options) {
            if (option instanceof DeferredSaveOption) {
                ((DeferredSaveOption<?>) option).save();
            }
        }

        this.setMode(this.mode);
        this.saveButton.active = false;
    }

    static Tooltip createColourListLabel(RainglowMode mode) {
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

    @Override
    public void closeScreen() {
        if (this.saveButton.active) {
            this.isConfirming = true;
            this.clearAndInit();
        } else {
            MinecraftClient.getInstance().setScreen(this.parent);
        }
    }

    @Override
    public void setConfirming(boolean confirming) {
        this.isConfirming = confirming;
    }

    @Override
    public void clearAndInit() {
        super.clearAndInit();
    }
}
