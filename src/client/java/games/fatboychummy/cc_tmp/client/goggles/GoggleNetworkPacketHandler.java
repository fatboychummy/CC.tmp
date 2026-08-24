package games.fatboychummy.cc_tmp.client.goggles;

import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.cc.SimpleWiredNetwork;
import games.fatboychummy.cc_tmp.client.goggles.render.GoggleRenderer;
import games.fatboychummy.cc_tmp.packet.GoggleNetworkPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class GoggleNetworkPacketHandler {
    public static void listen(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        SimpleWiredNetwork network = GoggleNetworkPacket.receive(buf);

        Cc_tmp.LOGGER.info("(client) GoggleNetworkPacket received: {}", network);

        GoggleRenderer.addNetwork(network);
    }
}
