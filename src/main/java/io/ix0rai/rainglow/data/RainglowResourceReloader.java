package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public interface RainglowResourceReloader extends SimpleSynchronousResourceReloadListener {
    default void log() {
        RainglowMode.printLoadedModes();
    }

    @Override
    default void reload(ResourceManager manager) {
        // remove existing modes to avoid adding duplicates
        // this only clears modes that exist on both the server and the client
        // otherwise we would have to re-request the mode data packet on every reload
        RainglowMode.clearUniversalModes();

        // run over all loaded resources and parse them to rainglow modes
        // then add them to our mode map
        for (Identifier id : manager.findResources("custom_modes", path -> path.endsWith(".json"))) {
            try (InputStream stream = manager.getResource(id).getInputStream()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                RainglowMode.JsonMode result = Rainglow.GSON.fromJson(reader, RainglowMode.JsonMode.class);
                RainglowMode.addMode(new RainglowMode(result, true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // log
        this.log();

        // load config
        if (Rainglow.CONFIG.isUninitialised() || !Rainglow.CONFIG.isEditLocked(MinecraftClient.getInstance())) {
            Rainglow.CONFIG.reloadFromFile();
            Rainglow.setMode(Rainglow.CONFIG.getMode());
        }
    }
}
