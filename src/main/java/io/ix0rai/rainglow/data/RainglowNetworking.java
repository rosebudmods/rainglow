package io.ix0rai.rainglow.data;

import io.ix0rai.rainglow.Rainglow;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

public class RainglowNetworking {
    public static final Identifier CONFIG_SYNC_ID = Rainglow.id("config_sync");
    public static final Identifier MODE_SYNC_ID = Rainglow.id("mode_sync");

    public static void syncConfig(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();

        // write current mode
        buf.writeString(Rainglow.CONFIG.getMode().getId());

        // write custom mode data
        List<String> colourIds = Rainglow.CONFIG.getCustom().stream().map(SquidColour::getId).toList();
        buf.writeCollection(colourIds, PacketByteBuf::writeString);

        // note: client does not need to know if server sync is enabled or not
        // they already know that it is enabled because they are receiving this packet

        ServerPlayNetworking.send(player, CONFIG_SYNC_ID, buf);
    }

    public static void sendModeData(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();

        Collection<RainglowMode> modes = RainglowMode.values();
        buf.writeCollection(modes, RainglowNetworking::writeMode);

        ServerPlayNetworking.send(player, MODE_SYNC_ID, buf);
    }

    public static Collection<RainglowMode> readModeData(PacketByteBuf buf) {
        return buf.readList(RainglowNetworking::readMode);
    }

    private static void writeMode(PacketByteBuf buf, RainglowMode mode) {
        buf.writeString(mode.getId());
        buf.writeText(mode.getText());
        List<String> colourIds = mode.getColours().stream().map(SquidColour::getId).toList();
        buf.writeCollection(colourIds, PacketByteBuf::writeString);
    }

    private static RainglowMode readMode(PacketByteBuf buf) {
        String id = buf.readString();
        Text text = buf.readText();
        List<String> colourIds = buf.readList(PacketByteBuf::readString);

        return new RainglowMode(id, colourIds, text, RainglowMode.byId(id) != null);
    }
}
