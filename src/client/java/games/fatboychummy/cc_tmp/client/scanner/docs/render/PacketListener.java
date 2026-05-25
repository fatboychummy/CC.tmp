package games.fatboychummy.cc_tmp.client.scanner.docs.render;

import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.client.scanner.PeripheralDocResolver;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDoc;
import games.fatboychummy.cc_tmp.packet.PacketHelper;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public abstract class PacketListener {
    public static void listen(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        String modId = PacketHelper.readString(buf);
        int peripheralTypeCount = buf.readInt();
        ArrayList<String> peripheralTypes = new ArrayList<>();

        for (int i = 0; i < peripheralTypeCount; i++) {
            peripheralTypes.add(PacketHelper.readString(buf));
        }

        PeripheralDoc[] docs = PeripheralDocResolver.getPeripheralDocs(modId, peripheralTypes.toArray(String[]::new));

        client.execute(() -> {
            if (docs == null || docs.length == 0) {
                client.setScreen(new ScannerScreen(Component.literal("No docs available.")));
                return;
            }
            client.setScreen(new ScannerScreen(Component.literal(docs[0].getPeripheralType())));
        });
    }
}
