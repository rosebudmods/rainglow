package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * this entire class is awful, and I'm embarrassed to have written it
 * if you're writing a pull request, please either rewrite this entire class or don't touch it
 * I'm sorry, but it's still better than night config
 * @author ix0rai
 */
public class RainglowConfig {
    private static final String CONFIG_FILE_NAME = "rainglow.toml";
    private static final Path CONFIG_FILE_PATH = Paths.get(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME).toUri());
    private static final String MODE_KEY = "mode";
    private static final String CUSTOM_KEY = "custom";

    private String mode;
    private List<SquidColour> custom;

    public RainglowConfig() {
        String content = "";

        try {
            content = Files.readString(CONFIG_FILE_PATH);
        } catch (IOException e) {
            Rainglow.LOGGER.info("config file not found or corrupted; creating new");
        } finally {
            // set mode and write to config file
            String readMode = getMode(content);
            List<SquidColour> readCustomColours = getCustom(content);
            this.mode = readMode;
            this.custom = readCustomColours;
            writeMode(readMode, false);
            writeCustomColours(readCustomColours, false);
        }
    }

    private static String getMode(String configContents) {
        // nasty tbh
        try {
            String modeLine = getLine(MODE_KEY, configContents);
            String modeName = modeLine.split("\"")[1].split("\"")[0];

            List<String> modeNames = new ArrayList<>();
            for (RainglowMode rainglowMode : RainglowMode.values()) {
                modeNames.add(rainglowMode.getName());
            }

            if (modeNames.contains(modeName)) {
                return modeName;
            } else {
                Rainglow.LOGGER.warn("parsed mode \"" + modeName + "\" from config is not valid; using default");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Rainglow.LOGGER.warn("could not parse mode from config; using default");
        }

        return RainglowMode.RAINBOW.getName();
    }

    private static List<SquidColour> getCustom(String configContents) {
        // disgusting tbh
        try {
            String customColourLine = getLine(CUSTOM_KEY, configContents);
            if (customColourLine.contains("[]")) {
                Rainglow.LOGGER.warn("no custom colours found in config; using default values");
            } else {
                String list = customColourLine.split("\\[")[1].split("]")[0];

                String[] colours = list.split(", ");
                List<SquidColour> customColours = new ArrayList<>();

                for (String colour : colours) {
                    // trim quotes
                    colour = colour.split("\"")[1].split("\"")[0];

                    // add colour
                    SquidColour squidColour = SquidColour.get(colour);
                    if (squidColour != null) {
                        customColours.add(squidColour);
                    } else {
                        Rainglow.LOGGER.warn("parsed custom colour \"" + colour + "\" from config is not valid; ignoring");
                    }
                }

                if (!customColours.isEmpty()) {
                    return customColours;
                }
            }
        } catch (Exception e) {
            Rainglow.LOGGER.warn("failed to parse custom colours from config; using default");
        }

        return List.of(SquidColour.BLUE, SquidColour.PINK, SquidColour.WHITE);
    }

    private void writeMode(String mode, boolean log) {
        try {
            write(MODE_KEY, "\"" + mode + "\"");
            if (log) {
                Rainglow.LOGGER.info("wrote mode \"" + mode + "\" to config file");
            }
        } catch (IOException e) {
            Rainglow.LOGGER.warn("could not write mode to config file!");
        }
    }

    private void writeCustomColours(List<SquidColour> customColours, boolean log) {
        try {
            // convert to toml-friendly format
            StringBuilder customColoursString = new StringBuilder();
            for (int i = 0; i < customColours.size(); i ++) {
                customColoursString.append("\"").append(customColours.get(i).getId()).append("\"").append(i == customColours.size() - 1 ? "" : ", ");
            }

            write(CUSTOM_KEY, "[" + customColoursString + "]");
            if (log) {
                Rainglow.LOGGER.info("wrote custom colours \"" + customColours + "\" to config file");
            }
        } catch (IOException e) {
            Rainglow.LOGGER.warn("could not write custom colours to config file!");
        }
    }

    private static void write(String key, String value) throws IOException {
        String content = Files.readString(CONFIG_FILE_PATH);
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i ++) {
            if (lines[i].startsWith(key)) {
                // if key is found replace line
                lines[i] = key + " = " + value;
                break;
            } else if (i == lines.length - 1) {
                // if key is not found append it to the end
                lines[i] += "\n" + key + " = " + value;
            }
        }

        Files.writeString(CONFIG_FILE_PATH, String.join("\n", lines));
    }

    private static String getLine(String key, String content) {
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (line.startsWith(key)) {
                return line;
            }
        }

        return "";
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
        this.writeMode(mode.getName(), true);
    }

    public void setCustom(List<SquidColour> custom) {
        this.custom = custom;
        // refresh custom mode if currently applied
        if (this.getMode() == RainglowMode.CUSTOM) {
            Rainglow.setMode(RainglowMode.CUSTOM);
        }
        this.writeCustomColours(custom, true);
    }

    public void addColourToCustom(SquidColour colour) {
        List<SquidColour> newCustom = this.getCustom();
        newCustom.add(colour);
        this.setCustom(newCustom);
    }

    public void removeColourFromCustom(SquidColour colour) {
        List<SquidColour> newCustom = this.getCustom();
        newCustom.remove(colour);
        this.setCustom(newCustom);
    }
}
