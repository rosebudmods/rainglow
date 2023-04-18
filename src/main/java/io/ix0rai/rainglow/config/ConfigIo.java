package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
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

    private ConfigIo() {
    }

    public static boolean parseTomlBoolean(String value) {
        return value.equals("true");
    }

    public static String parseTomlString(String string) {
        try {
            return string.split("\"")[1].split("\"")[0];
        } catch (Exception e) {
            Rainglow.LOGGER.warn("failed to parse toml string " + string + "; config will reset to default value");
            return "";
        }
    }

    public static List<String> parseTomlStringList(String list) {
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
        String content;
        try {
            content = Files.readString(CONFIG_FILE_PATH);
        } catch (IOException e) {
            Rainglow.LOGGER.warn("config file not found or corrupted; failed to read: creating new file with default values!");
            createConfigFile();
            return new HashMap<>();
        }

        String[] lines;
        try {
            lines = content.split("\n");
        } catch (Exception e) {
            Rainglow.LOGGER.warn("config file not found or corrupted; failed to read: creating new file with default values!");
            createConfigFile();
            return new HashMap<>();
        }

        Map<String, String> configData = new HashMap<>();

        for (String line : lines) {
            try {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] splitLine = line.split("=");

                configData.put(splitLine[0].trim(), splitLine[1].trim());
            } catch (Exception e) {
                Rainglow.LOGGER.warn("failed to read line \"" + line + "\" of config file; line will reset to default value");
            }
        }

        return configData;
    }

    public static void createConfigFile() {
        try {
            Files.createFile(CONFIG_FILE_PATH);
        } catch (IOException e) {
            Rainglow.LOGGER.warn("could not create config file!");
        }
    }

    public static void writeString(String key, String string) {
        write(key, "\"" + string + "\"", "string");
    }

    public static void writeBoolean(String key, boolean bool) {
        write(key, bool ? "true" : "false", "boolean");
    }

    public static void writeStringList(String key, List<?> list) {
        // convert to toml-friendly format
        StringBuilder tomlCompatibleList = new StringBuilder("[");
        for (int i = 0; i < list.size(); i ++) {
            tomlCompatibleList.append("\"").append(list.get(i).toString()).append("\"").append(i == list.size() - 1 ? "" : ", ");
        }
        tomlCompatibleList.append("]");

        write(key, tomlCompatibleList.toString(), "string list");
    }

    private static void write(String key, String value, String type) {
        try {
            String content = Files.readString(CONFIG_FILE_PATH);
            String[] lines = content.split("\n");

            for (int i = 0; i < lines.length; i++) {
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
        } catch (IOException e) {
            Rainglow.LOGGER.warn("could not write object " + value + " of type " + type + " to config file under key \"" + key + "\"!");
        }
    }
}
