package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface RainglowResourceReloader extends SimpleSynchronousResourceReloadListener {
    default void log() {
        RainglowMode.printLoadedModes();
    }

    @Override
    default void reload(ResourceManager manager) {
        Rainglow.RAINGLOW_DATAPACKS.clear();

        // load custom modes from rainglow/custom_modes in the datapack
        // we only load files whose name ends with .json
        Map<Identifier, Resource> map = manager.findResources("custom_modes", id -> id.getNamespace().equals(Rainglow.MOD_ID) && id.getPath().endsWith(".json"));

        // run over all loaded resources and parse them to rainglow modes
        // then add them to our mode map
        for (Map.Entry<Identifier, Resource> entry : map.entrySet()) {
            try (InputStream stream = entry.getValue().open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                RainglowMode.JsonMode result = Rainglow.GSON.fromJson(reader, RainglowMode.JsonMode.class);

                // todo logic for handling duplicates
                String name = entry.getValue().getSourceName();
                RainglowMode.addMode(new RainglowMode(result));

                if (this.getFabricId().equals(Rainglow.SERVER_MODE_DATA_ID) && !Rainglow.RAINGLOW_DATAPACKS.contains(name)) {
                    Rainglow.RAINGLOW_DATAPACKS.add(name);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.log();
    }
}
