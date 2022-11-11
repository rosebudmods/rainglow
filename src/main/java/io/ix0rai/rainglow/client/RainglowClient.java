package io.ix0rai.rainglow.client;

import io.ix0rai.rainglow.Rainglow;
import io.ix0rai.rainglow.data.RainglowMode;
import io.ix0rai.rainglow.data.RainglowResourceReloader;
import io.ix0rai.rainglow.data.SquidColour;
import io.ix0rai.rainglow.data.RainglowNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;

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

                // now that we have modes, we can load the config
                if (!Rainglow.CONFIG.isInitialised()) {
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
                if (Rainglow.CONFIG.isEditLocked()) {
                    // unlock config
                    Rainglow.CONFIG.setEditLocked(false);

                    // reset values to those configured in file
                    Rainglow.CONFIG.reloadFromFile();
                }
            })
        );

        // this is a bit of a hack, but as far as I can tell it's the only option
        // default modes need to be present on the server and the client
        // and since we store them in a datapack, that's impossible
        // a client connecting to a vanilla server would have no way to read the data
        // given that I haven't been able to find a way to access data packs from the client
        // maybe there's a solution
        // for now, this is what we're doing
        // I may investigate a better way in the future
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener((RainglowResourceReloader) () -> Rainglow.id("client_mode_data"));
    }
}
