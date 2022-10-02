package io.ix0rai.rainglow.config;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.option.SpruceBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomModeScreen extends SpruceScreen {
    private final Screen parent;

    private final SpruceOption clearOption;
    private final SpruceBooleanOption[] colourToggles = new SpruceBooleanOption[SquidColour.values().length];

    private final boolean[] toggleStates = new boolean[SquidColour.values().length];

    public CustomModeScreen(@Nullable Screen parent) {
        super(Rainglow.translatableText("config.title"));
        this.parent = parent;

        for (int i = 0; i < SquidColour.values().length; i ++) {
            final SquidColour colour = SquidColour.values()[i];
            final int index = i;

            toggleStates[index] = Rainglow.CONFIG.getCustom().contains(colour);

            colourToggles[index] = new SpruceBooleanOption(Rainglow.translatableTextKey("colour." + colour.getId()),
                    () -> toggleStates[index],
                    enable -> toggleStates[index] = enable,
                    null,
                    true
            );
        }

        this.clearOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.clear"),
            btn -> {
                for (int i = 0; i < SquidColour.values().length; i ++) {
                    toggleStates[i] = false;
                }

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
        for (int i = 0; i < SquidColour.values().length; i += 2) {
            SpruceOption secondToggle = null;
            if (i + 1 < SquidColour.values().length) {
                secondToggle = colourToggles[i + 1];
            }
            options.addOptionEntry(colourToggles[i], secondToggle);
        }
        this.addDrawableChild(options);

        this.addDrawableChild(this.clearOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
        this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150,
                buttonHeight, Rainglow.translatableText("config.save"),
                buttonWidget -> {
                    List<SquidColour> newCustom = new ArrayList<>();

                    for (int i = 0; i < SquidColour.values().length; i ++) {
                        if (toggleStates[i]) {
                            newCustom.add(SquidColour.values()[i]);
                        }
                    }

                    Rainglow.CONFIG.setCustom(newCustom);
                    this.closeScreen();
                }
        ));
    }
}
