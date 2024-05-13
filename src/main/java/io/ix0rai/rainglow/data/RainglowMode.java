package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
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
    private final List<RainglowColour> colours = new ArrayList<>();
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
            RainglowColour squidColour = RainglowColour.get(colour);
            if (squidColour == null) {
                Rainglow.LOGGER.warn("colour {} loaded from mode {} does not exist, skipping", colour, id);
                continue;
            }
            this.colours.add(RainglowColour.get(colour));
        }

        this.text = text;
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
        TextCodecs.UNLIMITED_TEXT_PACKET_CODEC.encode(buf, mode.getText());
        List<String> colourIds = mode.getColours().stream().map(RainglowColour::getId).toList();
        buf.writeCollection(colourIds, PacketByteBuf::writeString);
    }

    public static RainglowMode read(PacketByteBuf buf) {
        String id = buf.readString();
        Text text = TextCodecs.UNLIMITED_TEXT_PACKET_CODEC.decode(buf);
        List<String> colourIds = buf.readList(PacketByteBuf::readString);

        return new RainglowMode(id, colourIds, text, RainglowMode.get(id) != null);
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
