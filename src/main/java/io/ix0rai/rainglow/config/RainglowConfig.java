package io.ix0rai.rainglow.config;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RainglowConfig {
    public static final String MODE_KEY = "mode";
    public static final String CUSTOM_KEY = "custom";
    public static final String SERVER_SYNC_KEY = "enable_server_sync";

    public static final String RARITY_KEY = "rarity";
    public static final Function<RainglowEntity, String> TO_CONFIG_KEY = entity -> "enable_" + entity.getId();

    private RainglowMode mode;
    private List<RainglowColour> custom;
    private int rarity;
    private boolean enableServerSync;
    private boolean editLocked = false;
    private boolean isInitialised = false;
    private final Map<RainglowEntity, Boolean> entityToggles = new EnumMap<>(RainglowEntity.class);

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
        List<RainglowColour> customColours = new ArrayList<>();
        if (config.containsKey(CUSTOM_KEY)) {
            List<String> colours = ConfigIo.parseTomlStringList(config.get(CUSTOM_KEY));

            for (String colour : colours) {
                RainglowColour squidColour = RainglowColour.get(colour);
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

        // parse entity toggles
        for (RainglowEntity entity : RainglowEntity.values()) {
            String configKey = TO_CONFIG_KEY.apply(entity);

            if (config.containsKey(configKey)) {
                entityToggles.put(entity, ConfigIo.parseTomlBoolean(config.get(configKey)));
            } else {
                entityToggles.put(entity, true);
            }
        }

        // parse rarity
        int rarity = 100;
        if (config.containsKey(RARITY_KEY)) {
            rarity = ConfigIo.parseTomlInt(config.get(RARITY_KEY));
        }

        // reset colours if parsing failed
        if (customColours.isEmpty()) {
            customColours = RainglowMode.getDefaultCustom();
        }

        // set and write
        this.mode = rainglowMode;
        this.custom = customColours;
        this.enableServerSync = serverSync;
        this.rarity = rarity;
        this.save(false);

        this.isInitialised = true;
    }

    public RainglowMode getMode() {
        return this.mode;
    }

    public List<RainglowColour> getCustom() {
        return this.custom;
    }

    public int getRarity() {
        return this.rarity;
    }

    public boolean isServerSyncEnabled() {
        return this.enableServerSync;
    }

    public boolean isEditLocked(MinecraftClient client) {
        // client can only be locked inside a multiplayer server
        return !client.isInSingleplayer() && (client.getCurrentServerEntry() != null && this.editLocked);
    }

    public boolean isUninitialised() {
        return !this.isInitialised;
    }

    public void setMode(RainglowMode mode) {
        this.mode = mode;
        Rainglow.setMode(mode);
    }

    public void setCustom(List<RainglowColour> custom) {
        this.custom = custom;
        Rainglow.refreshColours();
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public void setEditLocked(boolean editLocked) {
        this.editLocked = editLocked;
    }

    public boolean isEntityEnabled(RainglowEntity entity) {
        return this.entityToggles.get(entity);
    }

    public void setEntityEnabled(RainglowEntity entity, boolean enabled) {
        this.entityToggles.put(entity, enabled);
    }

    public void save(boolean log) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || !this.isEditLocked(MinecraftClient.getInstance())) {
            ConfigIo.writeString(MODE_KEY, this.mode.getId());
            this.saveCustom();
            ConfigIo.writeBoolean(SERVER_SYNC_KEY, this.enableServerSync);
            ConfigIo.writeInt(RARITY_KEY, this.rarity);
        }

        // entity toggles cannot be locked by the server
        this.writeEntityToggles();
        if (log) {
            Rainglow.LOGGER.info("saved config!");
        }
    }

    public void saveCustom() {
        ConfigIo.writeStringList(CUSTOM_KEY, this.custom);
    }

    private void writeEntityToggles() {
        for (Map.Entry<RainglowEntity, Boolean> entry : entityToggles.entrySet()) {
            ConfigIo.writeBoolean(TO_CONFIG_KEY.apply(entry.getKey()), entry.getValue());
        }
    }
}
