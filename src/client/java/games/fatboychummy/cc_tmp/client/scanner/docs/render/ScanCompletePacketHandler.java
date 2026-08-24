package games.fatboychummy.cc_tmp.client.scanner.docs.render;

import games.fatboychummy.cc_tmp.client.scanner.PeripheralDocResolver;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDoc;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.DocParser;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.MarkdownParser;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.MarkdownVisitor;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.screen.ScannerScreen;
import games.fatboychummy.cc_tmp.packet.PacketHelper;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public abstract class ScanCompletePacketHandler {
    public static void listen(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        String modId = PacketHelper.readString(buf);
        int peripheralTypeCount = buf.readInt();
        ArrayList<String> peripheralTypes = new ArrayList<>();

        for (int i = 0; i < peripheralTypeCount; i++) {
            peripheralTypes.add(PacketHelper.readString(buf));
        }

        PeripheralDoc[] docs = PeripheralDocResolver.getPeripheralDocs(modId, peripheralTypes.toArray(String[]::new));

        client.execute(() -> {
            ScannerScreen screen;
            if (docs == null) {
                screen = new ScannerScreen(
                        MarkdownParser.parse(Component.translatable("gui.scan_finished.no_docs").getString())
                );
            } else if (docs.length == 0) {
                screen = new ScannerScreen(
                        MarkdownParser.parse(Component.translatable("gui.scan_finished.peripheral_no_docs").getString())
                );
            } else {
                screen = new ScannerScreen(
                        DocParser.parse(docs[0]) // Temporarily just grab the first doc.
                );
            }
            client.setScreen(screen);
        });
    }
}
