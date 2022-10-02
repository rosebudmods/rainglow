package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RainglowConfig {
    private String mode;
    private List<SquidColour> custom;

    public RainglowConfig() {
        Map<String, String> config = ConfigIo.readConfig();

        // set mode and write to config file
        String rainglowMode = RainglowMode.getDefault().getName();
        if (config.containsKey(ConfigIo.MODE_KEY)) {
            String parsedMode = ConfigIo.parseTomlString(config.get(ConfigIo.MODE_KEY));
            if (RainglowMode.byId(parsedMode).isPresent()) {
                rainglowMode = parsedMode;
            }
        }

        List<SquidColour> customColours = new ArrayList<>();
        if (config.containsKey(ConfigIo.CUSTOM_KEY)) {
            List<String> colours = ConfigIo.parseTomlList(config.get(ConfigIo.CUSTOM_KEY));

            for (String colour : colours) {
                if (SquidColour.get(colour) != null) {
                    customColours.add(SquidColour.get(colour));
                }
            }
        }

        if (customColours.isEmpty()) {
            customColours = getDefaultCustomColours();
        }

        this.mode = rainglowMode;
        this.custom = customColours;
        ConfigIo.writeMode(rainglowMode, false);
        ConfigIo.writeCustomColours(customColours, false);
    }

    private static List<SquidColour> getDefaultCustomColours() {
        return List.of(SquidColour.BLUE, SquidColour.PINK, SquidColour.WHITE);
    }

    public RainglowMode getMode() {
        return RainglowMode.byId(this.mode).orElse(RainglowMode.RAINBOW);
    }

    public List<SquidColour> getCustom() {
        return this.custom;
    }

    public void setMode(RainglowMode mode) {
        this.mode = mode.getName();
        Rainglow.setMode(mode);
        ConfigIo.writeMode(mode.getName(), true);
    }

    public void setCustom(List<SquidColour> custom) {
        this.custom = custom;
        // refresh custom mode if currently applied
        if (this.getMode() == RainglowMode.CUSTOM) {
            Rainglow.setMode(RainglowMode.CUSTOM);
        }
        ConfigIo.writeCustomColours(custom, true);
    }
}
