package games.fatboychummy.cc_tmp.packet;

import net.minecraft.network.FriendlyByteBuf;

import java.nio.charset.StandardCharsets;

public class PacketHelper {
    public static void writeString(FriendlyByteBuf buf, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readString(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
