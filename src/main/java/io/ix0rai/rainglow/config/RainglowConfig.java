package io.ix0rai.rainglow.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import io.ix0rai.rainglow.Rainglow;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RainglowConfig {
    private final CommentedFileConfig config;
    private static final String CONFIG_FILE_NAME = "rainglow.toml";
    private static final Path CONFIG_FILE_PATH = Paths.get("config/" + CONFIG_FILE_NAME);

    private static final String MODE_KEY = "mode";

    public RainglowConfig() {
        this.config = CommentedFileConfig.builder(CONFIG_FILE_PATH)
                .defaultResource("/" + CONFIG_FILE_NAME)
                .concurrent()
                .autoreload()
                .autosave()
                .build();
    }

    public RainglowMode getMode() {
        return RainglowMode.byId(config.getOrElse(MODE_KEY, RainglowMode.RAINBOW.getName())).orElse(RainglowMode.RAINBOW);
    }

    public void setMode(RainglowMode mode) {
        config.set(MODE_KEY, mode.getName());
        Rainglow.setMode(mode);
    }
}
