package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RainglowNetworking {
    public static void syncConfig(ServerPlayerEntity player) {
        // note: client does not need to know if server sync is enabled or not
        // they already know that it is enabled because they are receiving this packet
        ServerPlayNetworking.send(player, new ConfigSyncPayload(Rainglow.CONFIG.mode.value(), Rainglow.CONFIG.getCustom(), Rainglow.CONFIG.getToggles()));
    }

    public record ConfigSyncPayload(String currentMode, List<RainglowColour> customMode, Map<RainglowEntity, Boolean> enabledMobs) implements CustomPayload {
        public static final CustomPayload.Id<ConfigSyncPayload> PACKET_ID = new CustomPayload.Id<>(Rainglow.id("config_sync"));
        public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> PACKET_CODEC = PacketCodec.create(ConfigSyncPayload::write, ConfigSyncPayload::read);

        public void write(RegistryByteBuf buf) {
            buf.writeString(this.currentMode);
            buf.writeCollection(this.customMode, RainglowColour::write);
            buf.writeMap(this.enabledMobs, RainglowEntity::write, PacketByteBuf::writeBoolean);
        }

        public static ConfigSyncPayload read(RegistryByteBuf buf) {
            return new ConfigSyncPayload(
                    buf.readString(),
                    buf.readList(RainglowColour::read),
                    buf.readMap(RainglowEntity::read, PacketByteBuf::readBoolean)
            );
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }

    public static void syncModes(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new ModeSyncPayload(RainglowMode.values()));
    }

    public record ModeSyncPayload(Collection<RainglowMode> modes) implements CustomPayload {
        public static final CustomPayload.Id<ModeSyncPayload> PACKET_ID = new CustomPayload.Id<>(Rainglow.id("mode_sync"));
        public static final PacketCodec<RegistryByteBuf, ModeSyncPayload> PACKET_CODEC = PacketCodec.create(ModeSyncPayload::write, ModeSyncPayload::read);

        public void write(RegistryByteBuf buf) {
            buf.writeCollection(this.modes, RainglowMode::write);
        }

        public static ModeSyncPayload read(RegistryByteBuf buf) {
            return new ModeSyncPayload(
                    buf.readList(RainglowMode::read)
            );
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return PACKET_ID;
        }
    }
}
