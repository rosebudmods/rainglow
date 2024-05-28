package io.ix0rai.rainglow.client;

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowColour;
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
            if (!client.isIntegratedServerRunning()) {
                client.execute(() -> {
                    // custom must be set before mode so that if the server sends a custom mode it is set correctly
                    // otherwise the client's custom would be used
                    ValueList<String> customColours = ValueList.create("", payload.customMode().stream().map(RainglowColour::getId).toArray(String[]::new));
                    Rainglow.CONFIG.customColours.setOverride(customColours);
                    Rainglow.CONFIG.mode.setOverride(payload.currentMode());

                    var rarities = ValueMap.builder(0);
                    for (var entry : payload.rarities().entrySet()) {
                        rarities.put(entry.getKey().getId(), entry.getValue());
                    }
                    Rainglow.CONFIG.rarities.setOverride(rarities.build());

                    var toggles = ValueMap.builder(true);
                    for (var entry : payload.enabledMobs().entrySet()) {
                        toggles.put(entry.getKey().getId(), entry.getValue());
                    }
                    Rainglow.CONFIG.toggles.setOverride(toggles.build());

                    // log
                    Rainglow.LOGGER.info("received config from server: set mode to " + payload.currentMode() + " and custom colours to " + payload.customMode());
                });
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.ModeSyncPayload.PACKET_ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                List<String> newModeIds = new ArrayList<>();

                // add modes on the client
                for (RainglowMode mode : payload.modes()) {
                    newModeIds.add(mode.getId());
                    RainglowMode.addMode(mode);
                }


                // log
                if (!newModeIds.isEmpty()) {
                    Rainglow.LOGGER.info("received new modes from server: " + newModeIds);
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(() -> {
                // reset values to those configured in file
                Rainglow.CONFIG.values().forEach(TrackedValue::removeOverride);
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
