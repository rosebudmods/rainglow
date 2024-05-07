package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.SimpleOptionsScreen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomModeScreen extends SimpleOptionsScreen {
    private final SpruceOption clearOption;
    private final SpruceOption saveOption;
    private final SpruceBooleanOption[] colourToggles = new SpruceBooleanOption[RainglowColour.values().length];
    private final boolean[] toggleStates = new boolean[RainglowColour.values().length];

    public CustomModeScreen(@Nullable Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Rainglow.translatableText("config.title"),

        );

        // todo subclass option to allow saving via a save button
        // ephemeral value, not saved until a specific method is called (will happen on save pressed)

        // create toggles for each colour
        for (int i = 0; i < RainglowColour.values().length; i ++) {
            final RainglowColour colour = RainglowColour.values()[i];
            final int index = i;

            toggleStates[index] = Rainglow.CONFIG.getCustom().contains(colour);

            colourToggles[index] = new SpruceBooleanOption(Rainglow.translatableTextKey("colour." + colour.getId()),
                    () -> toggleStates[index],
                    enable -> toggleStates[index] = enable,
                    null,
                    true
            );
        }

        // toggles all colours to false
        this.clearOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.clear"),
            btn -> {
                for (int i = 0; i < RainglowColour.values().length; i ++) {
                    toggleStates[i] = false;
                }

                MinecraftClient client = MinecraftClient.getInstance();
                this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });

        // writes all the toggled colours to the config and reloads custom mode
        this.saveOption = SpruceSimpleActionOption.of(Rainglow.translatableTextKey("config.save"),
                buttonWidget -> {
                    List<RainglowColour> newCustom = new ArrayList<>();

                    for (int i = 0; i < RainglowColour.values().length; i ++) {
                        if (toggleStates[i]) {
                            newCustom.add(RainglowColour.values()[i]);
                        }
                    }

                    Rainglow.CONFIG.setCustom(newCustom);
                    Rainglow.CONFIG.saveCustom();
                    this.closeScreen();
                }
        );
    }

    @Override
    protected void init() {
        super.init();

        // create a list of toggles for each colour
        SpruceOptionListWidget options = new SpruceOptionListWidget(Position.of(0, 22), this.width, this.height - (35 + 22));
        for (int i = 0; i < RainglowColour.values().length; i += 2) {
            SpruceOption secondToggle = null;
            if (i + 1 < RainglowColour.values().length) {
                secondToggle = colourToggles[i + 1];
            }
            options.addOptionEntry(colourToggles[i], secondToggle);
        }
        this.addDrawableSelectableElement(options);

        // save and clear buttons
        this.addDrawableSelectableElement(this.clearOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
        this.addDrawableSelectableElement(this.saveOption.createWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150));
    }
}
