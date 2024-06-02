package io.ix0rai.rainglow.config;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Alias;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import io.ix0rai.rainglow.data.RainglowColour;
import io.ix0rai.rainglow.data.RainglowEntity;
import io.ix0rai.rainglow.data.RainglowMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class RainglowConfig extends ReflectiveConfig {
    @Comment("The currently active rainglow mode, which determines the possible colours for entities to spawn with.")
    @Comment("If custom, will be reset to the default mode if you join a server that does not have the mode.")
    @Alias("mode")
    public final TrackedValue<String> defaultMode = this.value("rainbow");
    @Comment("The rarity of coloured entities, with 0 making all entities vanilla and 100 making all entities coloured.")
    public final TrackedValue<ValueMap<Integer>> rarities = this.createMap(100);
    @Comment("Toggles for disabling colours for each entity.")
    public final TrackedValue<ValueMap<Boolean>> toggles = this.createMap(true);
    @Comment("The custom colours to use when the mode is set to custom.")
    public final TrackedValue<ValueList<String>> customColours = this.list("", RainglowMode.getDefaultCustom().stream().map(RainglowColour::getId).toArray(String[]::new));
    @Comment("Whether to allow recolouring entities via dyes.")
    public final TrackedValue<Boolean> allowDyeing = this.value(false);

    public List<RainglowColour> getCustom() {
        return this.customColours.value().stream().map(RainglowColour::get).collect(Collectors.toList());
    }

    public Map<RainglowEntity, Boolean> getToggles() {
        Map<RainglowEntity, Boolean> map = new HashMap<>();
        for (RainglowEntity entity : RainglowEntity.values()) {
            map.put(entity, this.isEntityEnabled(entity));
        }

        return map;
    }

    public Map<RainglowEntity, Integer> getRarities() {
        Map<RainglowEntity, Integer> map = new HashMap<>();
        for (RainglowEntity entity : RainglowEntity.values()) {
            map.put(entity, this.getRarity(entity));
        }

        return map;
    }

    public boolean isEntityEnabled(RainglowEntity entity) {
        return this.toggles.value().get(entity.getId());
    }

    public int getRarity(RainglowEntity entity) {
        return this.rarities.value().get(entity.getId());
    }

    /**
     * creates a map of default values for each {@link RainglowEntity}
     */
    private <T> TrackedValue<ValueMap<T>> createMap(T defaultValue) {
        var builder = this.map(defaultValue);
        for (RainglowEntity entity : RainglowEntity.values()) {
            builder.put(entity.getId(), defaultValue);
        }

        return builder.build();
    }
}
