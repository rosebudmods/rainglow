package io.ix0rai.rainglow.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.data.RainglowResourceReloader;
import io.ix0rai.rainglow.data.RainglowNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RainglowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.ConfigSyncPayload.PACKET_ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                // custom must be set before mode so that if the server sends a custom mode it is set correctly
                // otherwise the client's custom would be used
                Rainglow.CONFIG.setCustom(payload.customMode());
                Rainglow.CONFIG.setMode(RainglowMode.byId(payload.currentMode()));

                for (var entry : payload.enabledMobs().entrySet()) {
                    Rainglow.CONFIG.setEntityEnabled(entry.getKey(), entry.getValue());
                }

                // lock the config from reloading on resource reload
                Rainglow.CONFIG.setEditLocked(true);

                // log
                Rainglow.LOGGER.info("received config from server: set mode to " + payload.currentMode() + " and custom colours to " + payload.customMode());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.ModeSyncPayload.PACKET_ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                List<String> newModeIds = new ArrayList<>();

                // add modes that do not exist on the client to the map
                for (RainglowMode mode : payload.modes()) {
                    if (!mode.existsLocally()) {
                        newModeIds.add(mode.getId());
                        RainglowMode.addMode(mode);
                    }
                }

                // now that we have modes, we can load the config
                if (Rainglow.CONFIG.isUninitialised()) {
                    Rainglow.CONFIG.reloadFromFile();
                }

                // log
                if (!newModeIds.isEmpty()) {
                    Rainglow.LOGGER.info("received new modes from server: " + newModeIds);
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(() -> {
                if (Rainglow.CONFIG.isEditLocked(client)) {
                    // unlock config
                    Rainglow.CONFIG.setEditLocked(false);

                    // reset values to those configured in file
                    Rainglow.CONFIG.reloadFromFile();
                }
            })
        );

        // load default modes from the client
        // this is required to keep compatibility with vanilla servers, as they can't send their modes
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new RainglowResourceReloader() {
            @Override
            public Identifier getFabricId() {
                return Rainglow.id("client_mode_data");
            }

            @Override
            public void log() {
                Rainglow.LOGGER.info("loaded default modes");
            }
        });
    }
}
