package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RainglowConfig {
    private String mode;
    private static final String CONFIG_FILE_NAME = "rainglow.toml";
    private static final Path CONFIG_FILE_PATH = Paths.get("config/" + CONFIG_FILE_NAME);

    private static final String MODE_KEY = "mode";

    public RainglowConfig() {
        // default mode: rainbow
        String modeToSet = RainglowMode.RAINBOW.getName();

        try {
            // parse config file (badly)
            String content = Files.readString(CONFIG_FILE_PATH);
            String secondHalf = content.split(MODE_KEY + " = ")[1];
            modeToSet = secondHalf.split("\"")[1].split("\"")[0];
        } catch (IOException e) {
            Rainglow.LOGGER.info("config file not found; creating new");
        } catch (ArrayIndexOutOfBoundsException e) {
            Rainglow.LOGGER.warn("failed to read config file, using default mode (rainbow) and creating new config file");
        } finally {
            // set mode and write to config file
            this.mode = modeToSet;
            writeMode(modeToSet);
        }
    }

    private void writeMode(String mode) {
        try {
            Files.writeString(CONFIG_FILE_PATH, MODE_KEY + " = \"" + mode + "\"");
        } catch (IOException e) {
            Rainglow.LOGGER.warn("could not write to config file!");
        }
    }

    public RainglowMode getMode() {
        return RainglowMode.byId(this.mode).orElse(RainglowMode.RAINBOW);
    }

    public void setMode(RainglowMode mode) {
        this.mode = mode.getName();
        Rainglow.setMode(mode);
        this.writeMode(mode.getName());
    }
}
