package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum RainglowMode {
    RAINBOW(Rainglow.translatableText("mode.rainbow"), Formatting.LIGHT_PURPLE,
            SquidColour.RED,
            SquidColour.ORANGE,
            SquidColour.YELLOW,
            SquidColour.GREEN,
            SquidColour.BLUE,
            SquidColour.INDIGO,
            SquidColour.PURPLE
    ),
    ALL_COLOURS(Rainglow.translatableText("mode.all_colours"), Formatting.GREEN,
            SquidColour.values()
    ),
    TRANS_PRIDE(Rainglow.translatableText("mode.trans_pride"), Formatting.AQUA,
            SquidColour.BLUE,
            SquidColour.WHITE,
            SquidColour.PINK
    ),
    LESBIAN_PRIDE(Rainglow.translatableText("mode.lesbian_pride"), Formatting.RED,
            SquidColour.RED,
            SquidColour.ORANGE,
            SquidColour.WHITE,
            SquidColour.PINK,
            SquidColour.PURPLE
    ),
    BI_PRIDE(Rainglow.translatableText("mode.bi_pride"), Formatting.BLUE,
            SquidColour.BLUE,
            SquidColour.PINK,
            SquidColour.PURPLE
    ),
    GAY_PRIDE(Rainglow.translatableText("mode.gay_pride"), Formatting.DARK_AQUA,
            SquidColour.BLUE,
            SquidColour.GREEN,
            SquidColour.WHITE
    ),
    PAN_PRIDE(Rainglow.translatableText("mode.pan_pride"), Formatting.GOLD,
            SquidColour.PINK,
            SquidColour.YELLOW,
            SquidColour.BLUE
    ),
    ACE_PRIDE(Rainglow.translatableText("mode.ace_pride"), Formatting.DARK_GRAY,
            SquidColour.BLACK,
            SquidColour.GRAY,
            SquidColour.WHITE,
            SquidColour.PURPLE
    ),
    ARO_PRIDE(Rainglow.translatableText("mode.aro_pride"), Formatting.GREEN,
            SquidColour.BLACK,
            SquidColour.GRAY,
            SquidColour.WHITE,
            SquidColour.GREEN
    ),
    ENBY_PRIDE(Rainglow.translatableText("mode.enby_pride"), Formatting.DARK_PURPLE,
            SquidColour.YELLOW,
            SquidColour.WHITE,
            SquidColour.BLACK,
            SquidColour.PURPLE
    ),
    GENDERFLUID(Rainglow.translatableText("mode.genderfluid_pride"), Formatting.WHITE,
            SquidColour.PURPLE,
            SquidColour.WHITE,
            SquidColour.BLACK,
            SquidColour.PINK,
            SquidColour.BLUE
    ),
    MONOCHROME(Rainglow.translatableText("mode.monochrome"), Formatting.GRAY,
            SquidColour.BLACK,
            SquidColour.GRAY,
            SquidColour.WHITE
    ),
    VANILLA(Rainglow.translatableText("mode.vanilla"), Formatting.WHITE,
            SquidColour.BLUE
    );

    private final Text text;
    private final List<SquidColour> colours;

    RainglowMode(Text text, Formatting formatting, SquidColour... colours) {
        this.colours = List.of(colours);
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
