package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.SquidColour;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigIo {
    private static final String CONFIG_FILE_NAME = "rainglow.toml";
    private static final Path CONFIG_FILE_PATH = Paths.get(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME).toUri());

    public static final String MODE_KEY = "mode";
    public static final String CUSTOM_KEY = "custom";

    public static String parseTomlString(String string) {
        try {
            return string.split("\"")[1].split("\"")[0];
        } catch (Exception e) {
            Rainglow.LOGGER.warn("failed to parse toml string " + string + "; config will reset to default value");
            return "";
        }
    }

    public static List<String> parseTomlList(String list) {
        List<String> parsedList = new ArrayList<>();

        // trim brackets
        try {
            String rawList = list.split("\\[")[1].split("]")[0];
            // separate by comma
            String[] contents = rawList.split(",");

            for (String item : contents) {
                // trim
                item = item.trim();

                // trim quotes and add to list
                parsedList.add(item.split("\"")[1].split("\"")[0]);
            }
        } catch (Exception e) {
            Rainglow.LOGGER.error("failed to parse toml list " + list + "; config will reset to default value");
        }

        return parsedList;
    }

    public static Map<String, String> readConfig() {
        String content = "";
        try {
            content = Files.readString(CONFIG_FILE_PATH);
        } catch (IOException e) {
            Rainglow.LOGGER.warn("config file not found or corrupted; failed to read: config will reset to default value");
        }

        String[] lines = new String[0];
        try {
            lines = content.split("\n");
        } catch (Exception e) {
            Rainglow.LOGGER.warn("config file not found or corrupted; failed to read: config will reset to default value");
        }

        Map<String, String> configData = new HashMap<>();

        for (String line : lines) {
            try {
                String[] splitLine = line.split("=");

                configData.put(splitLine[0].trim(), splitLine[1].trim());
            } catch (Exception e) {
                Rainglow.LOGGER.warn("failed to read line \"" + line + "\" of config file; line will reset to default value");
            }
        }

        return configData;
    }

    public static void writeMode(String mode, boolean log) {
        try {
            write(MODE_KEY, "\"" + mode + "\"");
            if (log) {
                Rainglow.LOGGER.info("wrote mode \"" + mode + "\" to config file");
            }
        } catch (IOException e) {
            Rainglow.LOGGER.warn("could not write mode to config file!");
        }
    }

    public static void writeCustomColours(List<SquidColour> customColours, boolean log) {
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
}
