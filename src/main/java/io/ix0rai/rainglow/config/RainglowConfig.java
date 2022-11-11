package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.data.SquidColour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RainglowConfig {
    public static final String MODE_KEY = "mode";
    public static final String CUSTOM_KEY = "custom";
    public static final String SERVER_SYNC_KEY = "enable_server_sync";

    private RainglowMode mode;
    private List<SquidColour> custom;
    private boolean enableServerSync;

    private boolean editLocked = false;
    private boolean isInitialised = false;

    public RainglowConfig() {
        // we cannot load the config here because it would be loaded before modes, since it's statically initialised
    }

    public void reloadFromFile() {
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

        // parse server sync
        boolean serverSync = true;
        if (config.containsKey(SERVER_SYNC_KEY)) {
            serverSync = ConfigIo.parseTomlBoolean(config.get(SERVER_SYNC_KEY));
        }

        // reset colours if parsing failed
        if (customColours.isEmpty()) {
            customColours = RainglowMode.getDefaultCustom();
        }

        // set and write
        this.mode = rainglowMode;
        this.custom = customColours;
        this.enableServerSync = serverSync;
        ConfigIo.writeString(MODE_KEY, rainglowMode.getId(), false);
        ConfigIo.writeStringList(CUSTOM_KEY, customColours, false);
        ConfigIo.writeBoolean(SERVER_SYNC_KEY, serverSync, false);

        this.isInitialised = true;
    }

    public RainglowMode getMode() {
        return this.mode;
    }

    public List<SquidColour> getCustom() {
        return this.custom;
    }

    public boolean isServerSyncEnabled() {
        return this.enableServerSync;
    }

    public boolean isEditLocked() {
        return this.editLocked;
    }

    public boolean isInitialised() {
        return this.isInitialised;
    }

    public void setMode(RainglowMode mode, boolean write) {
        this.mode = mode;
        Rainglow.setMode(mode);
        if (write) {
            ConfigIo.writeString(MODE_KEY, mode.getId(), true);
        }
    }

    public void setCustom(List<SquidColour> custom, boolean write) {
        this.custom = custom;
        Rainglow.refreshColours();
        if (write) {
            ConfigIo.writeStringList(CUSTOM_KEY, custom, true);
        }
    }

    public void setEditLocked(boolean editLocked) {
        this.editLocked = editLocked;
    }
}
