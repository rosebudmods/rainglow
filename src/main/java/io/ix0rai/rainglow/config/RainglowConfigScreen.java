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
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RainglowConfigScreen extends Screen {
    private static final Text TITLE = Rainglow.translatableText("config.title");

    private final Screen parent;
    private final Map<RainglowEntity, DeferredSaveOption<Boolean>> toggles = new HashMap<>();
    private final Map<RainglowEntity, DeferredSaveOption<Integer>> sliders = new HashMap<>();
    private final ButtonWidget saveButton;

    private RainglowMode mode;
    private boolean isConfirming;

    public RainglowConfigScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
        this.mode = RainglowMode.get(Rainglow.CONFIG.mode.getRealValue());
        this.saveButton = ButtonWidget.builder(Rainglow.translatableText("config.save"), button -> {
            this.save();
            this.closeScreen(true);
        }).build();
        this.saveButton.active = false;
    }

    @Override
    public void init() {
        HeaderFooterLayoutWidget headerFooterWidget = new HeaderFooterLayoutWidget(this, 61, 33);
        LinearLayoutWidget headerLayout = headerFooterWidget.addToHeader(LinearLayoutWidget.createVertical().setSpacing(8));

        if (!this.isConfirming) {
            // header
            headerLayout.add(new TextWidget(TITLE, this.textRenderer), settings -> settings.alignHorizontallyCenter().alignVerticallyTop().setPadding(12));
            headerLayout.add(createModeButton(), LayoutSettings::alignVerticallyBottom);
            if (MinecraftClient.getInstance().world == null) {
                headerLayout.add(new TextWidget(Rainglow.translatableText("config.no_world"), this.textRenderer).setTextColor(0xc21919), LayoutSettings::alignHorizontallyCenter);
            }

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
            LinearLayoutWidget contentWidget = headerFooterWidget.addToContents(new LinearLayoutWidget(250, 100, LinearLayoutWidget.Orientation.VERTICAL).setSpacing(8));
            contentWidget.add(new TextWidget(Rainglow.translatableText("config.unsaved_warning"), this.textRenderer), LayoutSettings::alignHorizontallyCenter);

            LinearLayoutWidget buttons = new LinearLayoutWidget(250, 20, LinearLayoutWidget.Orientation.HORIZONTAL).setSpacing(8);
            buttons.add(ButtonWidget.builder(Rainglow.translatableText("config.continue_editing"), (buttonWidget) -> {
                this.isConfirming = false;
                this.clearAndInit();
            }).build());
            buttons.add(ButtonWidget.builder(CommonTexts.YES, (buttonWidget) -> MinecraftClient.getInstance().setScreen(this.parent)).build());

            contentWidget.add(buttons, LayoutSettings::alignHorizontallyCenter);
        }

        headerFooterWidget.visitWidgets(this::addDrawableSelectableElement);
        headerFooterWidget.arrangeElements();
    }

    private DeferredSaveOption<Boolean> createEntityToggle(RainglowEntity entity) {
        return toggles.computeIfAbsent(entity, e -> DeferredSaveOption.createDeferredBoolean(
                "config.enable_" + e.getId(),
                "tooltip.entity_toggle",
                Rainglow.CONFIG.toggles.getRealValue().get(e.getId()),
                enabled -> Rainglow.CONFIG.setEntityEnabled(e, enabled),
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
				rarity -> Rainglow.CONFIG.setRarity(e, rarity),
                rarity -> this.saveButton.active = true
		));
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
                            RainglowConfigScreen.this.mode = mode;
                        }
                );
    }

    private void save() {
        Collection<Option<?>> options = new ArrayList<>(this.sliders.values());
        options.addAll(this.toggles.values());

        for (Option<?> option : options) {
            if (option instanceof DeferredSaveOption) {
                ((DeferredSaveOption<?>) option).save();
            }
        }

        Rainglow.CONFIG.mode.setValue(this.mode.getId());
        Rainglow.setMode(RainglowMode.get(this.mode.getId()));
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

    @Override
    public void closeScreen() {
        this.closeScreen(false);
    }

    public void closeScreen(boolean saved) {
        if (!saved && this.saveButton.active) {
            this.isConfirming = true;
            this.clearAndInit();
        } else {
            if (Rainglow.CONFIG.isEditLocked(MinecraftClient.getInstance())) {
                sendConfigLockedToast();
            }

            MinecraftClient.getInstance().setScreen(this.parent);
        }
    }

    private static void sendConfigLockedToast() {
        Toast toast = new SystemToast(SystemToast.Id.PACK_LOAD_FAILURE, Rainglow.translatableText("config.server_locked_title"), Rainglow.translatableText("config.server_locked_description"));
        MinecraftClient.getInstance().getToastManager().add(toast);
    }
}
