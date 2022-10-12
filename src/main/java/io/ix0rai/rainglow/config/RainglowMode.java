package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RainglowMode {
    RAINBOW(TextColor.fromRgb(0xAA208F),
            SquidColour.RED,
            SquidColour.ORANGE,
            SquidColour.YELLOW,
            SquidColour.GREEN,
            SquidColour.BLUE,
            SquidColour.INDIGO,
            SquidColour.PURPLE
    ),
    ALL_COLOURS(TextColor.fromRgb(0x00FF00),
            SquidColour.values()
    ),
    TRANS_PRIDE(TextColor.fromRgb(0xD472E5),
            SquidColour.BLUE,
            SquidColour.WHITE,
            SquidColour.PINK
    ),
    LESBIAN_PRIDE(TextColor.fromRgb(0xDB4B32),
            SquidColour.RED,
            SquidColour.ORANGE,
            SquidColour.WHITE,
            SquidColour.PINK,
            SquidColour.PURPLE
    ),
    BI_PRIDE(TextColor.fromRgb(0x0063A0),
            SquidColour.BLUE,
            SquidColour.PINK,
            SquidColour.PURPLE
    ),
    GAY_PRIDE(TextColor.fromRgb(0x009391),
            SquidColour.BLUE,
            SquidColour.GREEN,
            SquidColour.WHITE
    ),
    PAN_PRIDE(TextColor.fromRgb(0xCEA800),
            SquidColour.PINK,
            SquidColour.YELLOW,
            SquidColour.BLUE
    ),
    ACE_PRIDE(TextColor.fromRgb(0xA252BF),
            SquidColour.BLACK,
            SquidColour.GRAY,
            SquidColour.WHITE,
            SquidColour.PURPLE
    ),
    ARO_PRIDE(TextColor.fromRgb(0x61D85B),
            SquidColour.BLACK,
            SquidColour.GRAY,
            SquidColour.WHITE,
            SquidColour.GREEN
    ),
    ENBY_PRIDE(TextColor.fromRgb(0x705CA8),
            SquidColour.YELLOW,
            SquidColour.WHITE,
            SquidColour.BLACK,
            SquidColour.PURPLE
    ),
    GENDERFLUID(TextColor.fromRgb(0xA02CB7),
            SquidColour.PURPLE,
            SquidColour.WHITE,
            SquidColour.BLACK,
            SquidColour.PINK,
            SquidColour.BLUE
    ),
    MONOCHROME(TextColor.fromRgb(0xB7B7B7),
            SquidColour.BLACK,
            SquidColour.GRAY,
            SquidColour.WHITE
    ),
    VANILLA(TextColor.fromRgb(0xFFFFFF),
            SquidColour.BLUE
    ),
    CUSTOM(TextColor.fromRgb(0x00FFE1));

    private static final Map<String, RainglowMode> BY_ID = new HashMap<>();

    static {
       Arrays.stream(values()).forEach(mode -> BY_ID.put(mode.getId(), mode));
    }

    private final Text text;
    private final List<SquidColour> colours;

    RainglowMode(TextColor formatting, SquidColour... colours) {
        this.colours = Arrays.stream(colours).toList();
        this.text = Rainglow.translatableText("mode." + this.getId()).copy().setStyle(Style.EMPTY.withColor(formatting));
    }

    public List<SquidColour> getColours() {
        // custom colours are handled by the config instead of the enum
        if (this == CUSTOM) {
            return Rainglow.CONFIG.getCustom();
        } else {
            return this.colours;
        }
    }

    public RainglowMode cycle() {
        // cycle to next in list, wrapping around to 0 if the next ordinal is larger than the enum's size
        return values()[this.ordinal() + 1 >= values().length ? 0 : this.ordinal() + 1];
    }

    public Text getText() {
        return this.text;
    }

    public String getId() {
        return this.name().toLowerCase();
    }

    public static RainglowMode byId(String id) {
        return BY_ID.get(id);
    }

    public static RainglowMode getDefault() {
        return RAINBOW;
    }

    public static List<SquidColour> getDefaultCustom() {
        return TRANS_PRIDE.getColours();
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
