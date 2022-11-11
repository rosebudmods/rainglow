package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RainglowMode {
    private static final SortedMap<String, RainglowMode> MODES = new TreeMap<>();

    private final String id;
    private final List<SquidColour> colours = new ArrayList<>();
    private final Text text;
    private final boolean existsLocally;

    public RainglowMode(JsonMode mode, boolean existsLocally) {
        this(
                mode.id,
                mode.colourIds,
                Rainglow.translatableText("mode." + mode.id).copy().setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Integer.parseInt(mode.textColour, 16)))),
                existsLocally
        );
    }

    public RainglowMode(String id, List<String> colourIds, Text text, boolean existsLocally) {
        if (!id.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException("loaded rainglow mode with id " + id + " which contains invalid characters");
        }

        this.id = id;

        for (String colour : colourIds) {
            SquidColour squidColour = SquidColour.get(colour);
            if (squidColour == null) {
                Rainglow.LOGGER.warn("colour {} loaded from mode {} does not exist, skipping", colour, id);
                continue;
            }
            this.colours.add(SquidColour.get(colour));
        }

        this.text = text;
        this.existsLocally = existsLocally;

        MODES.put(this.id, this);
    }

    public List<SquidColour> getColours() {
        // custom colours are handled by the config instead of the enum
        // all colours mode is handled through code so that I don't have to update if new colours are added
        if (this.isCustom()) {
            return Rainglow.CONFIG.getCustom();
        } else if (this.getId().equals("all_colours")) {
            return List.of(SquidColour.values());
        } else {
            return this.colours;
        }
    }

    public RainglowMode cycle() {
        // cycle to next in list, wrapping around to 0 if the next ordinal is larger than the map's size
        Collection<RainglowMode> values = MODES.values();
        Iterator<RainglowMode> iterator = values.iterator();

        // look for matching key and return next mode
        while (iterator.hasNext()) {
            RainglowMode mode = iterator.next();
            if (mode.id.equals(this.id) && iterator.hasNext()) {
                return iterator.next();
            }
        }

        // otherwise return first mode
        return values.iterator().next();
    }

    public Text getText() {
        return this.text;
    }

    public boolean isCustom() {
        return this.getId().equals("custom");
    }

    public String getId() {
        return this.id;
    }

    public boolean existsLocally() {
        return this.existsLocally;
    }

    public static RainglowMode byId(String id) {
        return MODES.get(id);
    }

    public static RainglowMode getDefault() {
        return MODES.get("rainbow");
    }

    public static void addMode(RainglowMode mode) {
        MODES.put(mode.id, mode);
    }

    public static void clearUniversalModes() {
        Collection<RainglowMode> modes = List.copyOf(MODES.values());

        for (RainglowMode mode : modes) {
            if (mode.existsLocally()) {
                MODES.remove(mode.id);
            }
        }
    }

    public static Collection<RainglowMode> values() {
        return MODES.values();
    }

    public static List<SquidColour> getDefaultCustom() {
        return List.of(SquidColour.BLUE, SquidColour.WHITE, SquidColour.PINK);
    }

    public static void printLoadedModes() {
        Rainglow.LOGGER.info("loaded modes: " + MODES);
    }

    @Override
    public String toString() {
        return this.getId();
    }

    public static class JsonMode {
        public String id;
        public List<String> colourIds;
        public String textColour;

        public JsonMode(String id, List<String> colourIds, String textColour) {
            this.id = id;
            this.colourIds = colourIds;
            this.textColour = textColour;
        }

        @Override
        public String toString() {
            return "Mode{" +
                    "id='" + id + '\'' +
                    ", colourIds=" + colourIds +
                    ", textColour='" + textColour + '\'' +
                    '}';
        }
    }
}
