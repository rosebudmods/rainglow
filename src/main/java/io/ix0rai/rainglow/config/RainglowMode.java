package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum RainglowMode {
    RAINBOW(Rainglow.translatableText("mode.rainbow"), Formatting.LIGHT_PURPLE, List.of(
            SquidColour.RED,
            SquidColour.ORANGE,
            SquidColour.YELLOW,
            SquidColour.GREEN,
            SquidColour.BLUE,
            SquidColour.INDIGO,
            SquidColour.PURPLE
    )),
    ALL_COLOURS(Rainglow.translatableText("mode.all_colours"), Formatting.GREEN, List.of(
            SquidColour.values()
    )),
    TRANS_PRIDE(Rainglow.translatableText("mode.trans_pride"), Formatting.AQUA, List.of(
            SquidColour.BLUE,
            SquidColour.WHITE,
            SquidColour.PINK
    )),
    LESBIAN_PRIDE(Rainglow.translatableText("mode.lesbian_pride"), Formatting.DARK_PURPLE, List.of(
            SquidColour.RED,
            SquidColour.ORANGE,
            SquidColour.WHITE,
            SquidColour.PINK,
            SquidColour.PURPLE
    ));

    private final Text text;
    private final List<SquidColour> colours;

    RainglowMode(Text text, Formatting formatting, List<SquidColour> colours) {
        this.colours = colours;
        this.text = text.copy().formatted(formatting);
    }

    public List<SquidColour> getColours() {
        return this.colours;
    }

    public RainglowMode next() {
        if (values().length == this.ordinal() + 1) {
            return values()[0];
        }
        return values()[this.ordinal() + 1];
    }

    public Text getTranslatedText() {
        return this.text;
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    public static Optional<RainglowMode> byId(String id) {
        return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
    }
}
