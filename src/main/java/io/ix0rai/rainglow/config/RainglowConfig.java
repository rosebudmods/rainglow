package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RainglowConfig {
    public static final String MODE_KEY = "mode";
    public static final String CUSTOM_KEY = "custom";

    private RainglowMode mode;
    private List<SquidColour> custom;

    public RainglowConfig() {
        // read config from file
        Map<String, String> config = ConfigIo.readConfig();

        // parse mode
        RainglowMode rainglowMode = RainglowMode.getDefault();
        if (config.containsKey(MODE_KEY)) {
            RainglowMode parsedMode = RainglowMode.byId(ConfigIo.parseTomlString(config.get(MODE_KEY)));
            if (parsedMode != null) {
                rainglowMode = parsedMode;
            }
        }

        // parse colours for custom mode
        // note: we cannot get the default colours from the enum to start off as it's an immutable list
        List<SquidColour> customColours = new ArrayList<>();
        if (config.containsKey(CUSTOM_KEY)) {
            List<String> colours = ConfigIo.parseTomlStringList(config.get(CUSTOM_KEY));

            for (String colour : colours) {
                SquidColour squidColour = SquidColour.get(colour);
                if (squidColour != null) {
                    customColours.add(squidColour);
                }
            }
        }

        // reset colours if parsing failed
        if (customColours.isEmpty()) {
            customColours = RainglowMode.getDefaultCustom();
        }

        // set and write
        this.mode = rainglowMode;
        this.custom = customColours;
        ConfigIo.writeString(MODE_KEY, rainglowMode.getId(), false);
        ConfigIo.writeStringList(CUSTOM_KEY, customColours, false);
    }

    public RainglowMode getMode() {
        return this.mode;
    }

    public List<SquidColour> getCustom() {
        return this.custom;
    }

    public void setMode(RainglowMode mode) {
        this.mode = mode;
        Rainglow.setMode(mode);
        ConfigIo.writeString(MODE_KEY, mode.getId(), true);
    }

    public void setCustom(List<SquidColour> custom) {
        this.custom = custom;
        Rainglow.refreshColours();
        ConfigIo.writeStringList(CUSTOM_KEY, custom, true);
    }
}
