package games.fatboychummy.cc_tmp.packet;

import games.fatboychummy.cc_tmp.cc.ConnectionType;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

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

    public static void writeVec3i(FriendlyByteBuf buf, Vec3i vec) {
        buf.writeInt(vec.getX());
        buf.writeInt(vec.getY());
        buf.writeInt(vec.getZ());
    }

    public static Vec3i readVec3i(FriendlyByteBuf buf) {
        return new Vec3i(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void writeConnectionType(FriendlyByteBuf buf, ConnectionType type) {
        buf.writeInt(type.ordinal());
    }

    public static ConnectionType readConnectionType(FriendlyByteBuf buf) {
        return ConnectionType.values()[buf.readInt()];
    }
}
