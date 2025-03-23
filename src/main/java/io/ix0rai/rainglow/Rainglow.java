package io.ix0rai.rainglow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import folk.sisby.kaleido.lib.quiltconfig.api.serializers.TomlSerializer;
import folk.sisby.kaleido.lib.quiltconfig.implementor_api.ConfigEnvironment;
import io.ix0rai.rainglow.config.PerWorldConfig;
import io.ix0rai.rainglow.config.RainglowConfig;
import io.ix0rai.rainglow.data.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Rainglow implements ModInitializer {
    public static final String MOD_ID = "rainglow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String FORMAT = "toml";
    private static final ConfigEnvironment ENVIRONMENT = new ConfigEnvironment(FabricLoader.getInstance().getConfigDir(), FORMAT, TomlSerializer.INSTANCE);
    public static final RainglowConfig CONFIG = RainglowConfig.create(ENVIRONMENT, "", MOD_ID, RainglowConfig.class);
    public static final PerWorldConfig MODE_CONFIG = PerWorldConfig.create(ENVIRONMENT, MOD_ID, "per_world", PerWorldConfig.class);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String CUSTOM_NBT_KEY = "Colour";
    public static final Identifier SERVER_MODE_DATA_ID = id("server_mode_data");
    public static final List<String> RAINGLOW_DATAPACKS = new ArrayList<>();

    private static final Map<UUID, RainglowColour> COLOURS = new HashMap<>();

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener((RainglowResourceReloader) () -> SERVER_MODE_DATA_ID);

        PayloadTypeRegistry.playS2C().register(RainglowNetworking.ConfigSyncPayload.PACKET_ID, RainglowNetworking.ConfigSyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RainglowNetworking.ModeSyncPayload.PACKET_ID, RainglowNetworking.ModeSyncPayload.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(RainglowNetworking.ColourPayload.PACKET_ID, RainglowNetworking.ColourPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RainglowNetworking.ColourPayload.PACKET_ID, RainglowNetworking.ColourPayload.PACKET_CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // send modes to client
            RainglowNetworking.syncModes(handler.player);

            // send config to client
            RainglowNetworking.syncConfig(handler.player);

            // send all colours to client
            RainglowNetworking.sendColoursTo(handler.player);
        });

        ServerPlayNetworking.registerGlobalReceiver(RainglowNetworking.ColourPayload.PACKET_ID, (payload, context) -> {
            for (var entry : payload.colours().entrySet()) {
                RainglowColour colour = entry.getValue();
                Rainglow.setColour(entry.getKey(), colour);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            // Only clear colours on disconnect if server is NOT single-player to prevent NBT save failure (Unsure how this works with Lan-instances)
            if (!server.isSingleplayer()) {
                COLOURS.clear();
            }
        });

        // Instead use SERVER_STOPPED for clearing colours from single-player worlds. (Doesn't affect others because mod would most likely be shutdown in non-single-player instances)
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> COLOURS.clear());
    }

    public static Identifier id(String id) {
        return Identifier.of(MOD_ID, id);
    }

    public static RainglowColour generateRandomColour(World world, RandomGenerator random) {
        var colours = MODE_CONFIG.getMode(world).getColours();
        return colours.get(random.nextInt(colours.size()));
    }

    public static boolean colourUnloaded(World world, RainglowEntity entityType, RainglowColour colour) {
        var colours = MODE_CONFIG.getMode(world).getColours();
        return !colours.contains(colour) && colour != entityType.getDefaultColour();
    }

    public static String translatableTextKey(String key) {
        if (key.split("\\.").length < 2) throw new IllegalArgumentException("key must be in format \"category.key\": " + key);
        return MOD_ID + "." + key;
    }

    public static Text translatableText(String key, Object... args) {
        return Text.translatable(translatableTextKey(key), args);
    }

    public static Text translatableText(String key) {
        return Text.translatable(translatableTextKey(key));
    }

    public static RainglowColour getColour(Entity entity) {
        RainglowColour colour = COLOURS.get(entity.getUuid());
        RainglowEntity entityType = RainglowEntity.get(entity);

        // generate random colour if the squid's colour isn't currently loaded
        if (colourUnloaded(entity.getWorld(), entityType, colour)) {
            // Use last generated colour if not null else generate a new colour
            colour = generateRandomColour(entity.getWorld(), entity.getRandom());
            COLOURS.put(entity.getUuid(), colour);
        }

        return colour;
    }

    // Simplified method without any colour checks (Entity information isn't being passed through Rendering anymore, can be adjusted to apply in the RenderStateInteract if needed)
    public static RainglowColour getColour(UUID entity) {
        return COLOURS.get(entity);
    }

    public static void setColour(Entity entity, RainglowColour colour) {
        setColour(entity.getUuid(), colour);

        if (entity.getWorld().isClient()) {
            // sync to server; will then be synced to all clients
            RainglowNetworking.sendColourChangeToServer(entity, colour);
        } else if (entity.getWorld().getServer().isDedicated()) {
            // sync to all clients
            RainglowNetworking.sendColourChangeToClients(entity, colour);
        }
    }

    public static void setColour(UUID uuid, RainglowColour colour) {
        COLOURS.put(uuid, colour);
    }

    public static Map<UUID, RainglowColour> getColours() {
        return COLOURS;
    }
}
