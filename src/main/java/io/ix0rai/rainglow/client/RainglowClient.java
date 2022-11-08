package io.ix0rai.rainglow.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.RainglowMode;
import io.ix0rai.rainglow.SquidColour;
import io.ix0rai.rainglow.networking.RainglowNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RainglowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.CONFIG_SYNC_ID, (client, handler, buf, responseSender) -> {
            String mode = buf.readString();

            List<String> colourIds = buf.readList(PacketByteBuf::readString);
            List<SquidColour> colours = colourIds.stream().map(SquidColour::get).toList();

            client.execute(() -> {
                // custom must be set before mode so that if the server sends a custom mode it is set correctly
                // otherwise the client's custom would be used
                Rainglow.CONFIG.setCustom(colours, false);
                Rainglow.CONFIG.setMode(RainglowMode.byId(mode), false);

                // lock the config from reloading on resource reload
                Rainglow.CONFIG.setEditLocked(true);

                // log
                Rainglow.LOGGER.info("received config from server: set mode to " + mode + " and custom colours to " + colourIds);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.MODE_SYNC_ID, (client, handler, buf, responseSender) -> {
            Collection<RainglowMode> modes = RainglowNetworking.readModeData(buf);

            client.execute(() -> {
                List<String> newModeIds = new ArrayList<>();

                // add modes that do not exist on the client to the map
                for (RainglowMode mode : modes) {
                    if (!mode.existsLocally()) {
                        newModeIds.add(mode.getId());
                        RainglowMode.addMode(mode);
                    }
                }

                // log
                if (!newModeIds.isEmpty()) {
                    Rainglow.LOGGER.info("received new modes from server: " + newModeIds);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RainglowNetworking.UNLOCK_CONFIG_ID, (client, handler, buf, responseSender) ->
            client.execute(() -> Rainglow.CONFIG.setEditLocked(false))
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
            client.execute(() -> {
                // unlock config
                Rainglow.CONFIG.setEditLocked(false);

                // reset values to those configured in file
                Rainglow.CONFIG.reloadFromFile();
            })
        );
    }
}
