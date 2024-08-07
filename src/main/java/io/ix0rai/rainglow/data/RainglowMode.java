package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RainglowMode {
    private static final SortedMap<String, RainglowMode> MODES = new TreeMap<>();

    private final String id;
    private final List<RainglowColour> colours = new ArrayList<>();
    private final Text text;
    private final boolean existsLocally;

    public RainglowMode(JsonMode mode, boolean existsLocally) {
        this(
                mode.id,
                mode.colourIds,
                existsLocally
        );
    }

    public RainglowMode(String id, List<String> colourIds, boolean existsLocally) {
        if (!id.matches("^[a-z0-9_]+$")) {
            Rainglow.LOGGER.error("loaded rainglow mode with id {} which contains invalid characters! (only lowercase letters, numbers, and underscores are allowed)", id);
        }

        this.id = id;

        for (String colour : colourIds) {
            RainglowColour squidColour = RainglowColour.get(colour);
            if (squidColour == null) {
                Rainglow.LOGGER.warn("colour {} loaded from mode {} does not exist, skipping", colour, id);
                continue;
            }
            this.colours.add(RainglowColour.get(colour));
        }

        if (this.colours.isEmpty() && !id.equals("all_colours") && !id.equals("custom")) {
            Rainglow.LOGGER.error("cannot load mode with id {}: no colours found!", id);
        }

        // todo possible improvements: split remainder between first and last section, ignore spaces in char count and sections, split sections at spaces
        if (!colours.isEmpty()) {
            String fullText = Language.getInstance().get(Rainglow.translatableTextKey("mode." + id));
            int textLength = fullText.length();
            int charsPerSection = textLength / colours.size();
            int extraCharsOnLastSection = textLength % colours.size();

            MutableText formatted = Text.empty();
            for (int i = 0; i < colours.size(); i++) {
                int start = i * charsPerSection;
                int end = start + charsPerSection + (i == colours.size() - 1 ? extraCharsOnLastSection : 0);
                formatted.append(Text.literal(fullText.substring(start, end)).setStyle(Style.EMPTY.withColor((colours.get(i).getHex()))));
            }

            this.text = formatted;
        } else {
            this.text = Rainglow.translatableText("mode." + id);
        }
        this.existsLocally = existsLocally;

        MODES.put(this.id, this);
    }

    public List<RainglowColour> getColours() {
        // custom colours are handled by the config instead of the enum
        // all colours mode is handled through code so that I don't have to update if new colours are added
        return switch (this.getId()) {
            case "custom" -> Rainglow.CONFIG.getCustom();
            case "all_colours" -> List.of(RainglowColour.values());
            default -> this.colours;
        };
    }

    @Override
    public String toString() {
        return this.getId();
    }

    public Text getText() {
        return this.text;
    }

    public String getId() {
        return this.id;
    }

    public boolean existsLocally() {
        return this.existsLocally;
    }

    public static RainglowMode get(String id) {
        return MODES.get(id);
    }

    public static void addMode(RainglowMode mode) {
        MODES.put(mode.id, mode);
    }

    public static Collection<RainglowMode> values() {
        return MODES.values();
    }

    public static List<RainglowColour> getDefaultCustom() {
        return List.of(RainglowColour.BLUE, RainglowColour.WHITE, RainglowColour.PINK);
    }

    public static void clearUniversalModes() {
        Collection<RainglowMode> modes = List.copyOf(MODES.values());
        for (RainglowMode mode : modes) if (mode.existsLocally()) MODES.remove(mode.id);
    }

    public static void printLoadedModes() {
        StringBuilder formatted = new StringBuilder();
        for (RainglowMode mode : MODES.values())
        {
            // custom may be null since colour data is loaded pre-config
            int colourCount = mode.getColours() != null ? mode.getColours().size() : 0;
            formatted.append(mode.getId()).append(" (").append(colourCount).append(" colours), ");
        }

        // remove trailing space and comma
        formatted.append("\b\b");
        Rainglow.LOGGER.info("Loaded modes: [" + formatted + "]");
    }

    public static void write(PacketByteBuf buf, RainglowMode mode) {
        buf.writeString(mode.getId());
        List<String> colourIds = mode.getColours().stream().map(RainglowColour::getId).toList();
        buf.writeCollection(colourIds, PacketByteBuf::writeString);
    }

    public static RainglowMode read(PacketByteBuf buf) {
        String id = buf.readString();
        List<String> colourIds = buf.readList(PacketByteBuf::readString);

        return new RainglowMode(id, colourIds, RainglowMode.get(id) != null);
    }


    /**
     * represents modes loaded from json files
     * these are always converted to RainglowMode objects before use
     */
    public static class JsonMode {
        public String id;
        public List<String> colourIds;
        public String textColour;

        public JsonMode(String id, List<String> colourIds, String textColour) {
            this.id = id;
            this.colourIds = colourIds;
            this.textColour = textColour;
        }
    }
}
