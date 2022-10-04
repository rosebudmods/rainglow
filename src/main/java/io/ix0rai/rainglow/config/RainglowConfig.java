package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RainglowConfig {
    private RainglowMode mode;
    private List<SquidColour> custom;

    public RainglowConfig() {
        Map<String, String> config = ConfigIo.readConfig();

        // set mode and write to config file
        RainglowMode rainglowMode = RainglowMode.getDefault();
        if (config.containsKey(ConfigIo.MODE_KEY)) {
            RainglowMode parsedMode = RainglowMode.byId(ConfigIo.parseTomlString(config.get(ConfigIo.MODE_KEY)));
            if (parsedMode != null) {
                rainglowMode = parsedMode;
            }
        }

        List<SquidColour> customColours = new ArrayList<>();
        if (config.containsKey(ConfigIo.CUSTOM_KEY)) {
            List<String> colours = ConfigIo.parseTomlList(config.get(ConfigIo.CUSTOM_KEY));

            for (String colour : colours) {
                SquidColour squidColour = SquidColour.get(colour);
                if (squidColour != null) {
                    customColours.add(squidColour);
                }
            }
        }

        if (customColours.isEmpty()) {
            customColours = RainglowMode.getDefaultCustom();
        }

        this.mode = rainglowMode;
        this.custom = customColours;
        ConfigIo.writeMode(rainglowMode.getName(), false);
        ConfigIo.writeCustomColours(customColours, false);
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
        ConfigIo.writeMode(mode.getName(), true);
    }

    public void setCustom(List<SquidColour> custom) {
        this.custom = custom;
        Rainglow.refreshColours();
        ConfigIo.writeCustomColours(custom, true);
    }
}
