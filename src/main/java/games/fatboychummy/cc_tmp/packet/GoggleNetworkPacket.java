package games.fatboychummy.cc_tmp.packet;

import dan200.computercraft.api.network.wired.WiredNode;
import games.fatboychummy.cc_tmp.cc.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class GoggleNetworkPacket {
    private final ServerPlayer player;
    private final SimpleWiredNetwork network;

    public GoggleNetworkPacket(ServerPlayer player, WiredNode node) {
        this.player = player;
        this.network = SimpleWiredNetwork.computeNetwork(
                node.getElement().getLevel().dimension().location().toString(),
                node.getNetwork()
        );
    }

    public static SimpleWiredNetwork receive(FriendlyByteBuf buf) {
        String dimension = PacketHelper.readString(buf);
        SimpleWiredNetwork network = new SimpleWiredNetwork(0x5500ff00, dimension);

        int nodeCount = buf.readInt();
        int peripheralCount = buf.readInt();
        int nodeConnectionCount = buf.readInt();
        int peripheralConnectionCount = buf.readInt();

        // We pre-allocate these as we will need to work them together with connections later.
        List<SimpleWiredNode> nodes = new ArrayList<>(nodeCount);
        List<PeripheralNode> peripherals = new ArrayList<>(peripheralCount);

        for (int i = 0; i < nodeCount; i++) {
            nodes.add(new SimpleWiredNode(PacketHelper.readVec3i(buf), new ArrayList<>()));
        }
        for (int i = 0; i < peripheralCount; i++) {
            peripherals.add(new PeripheralNode(
                    PacketHelper.readVec3i(buf), // Order is important, Position of peripheral, position of origin, *then* name.
                    PacketHelper.readVec3i(buf),
                    PacketHelper.readString(buf)
            ));
        }
        for (int i = 0; i < peripheralConnectionCount; i++) {
            int from = buf.readInt();
            int to = buf.readInt();
            nodes.get(from).addPeripheral(peripherals.get(to));
        }

        for (SimpleWiredNode node : nodes) {
            network.addNode(node);
        }

        for (int i = 0; i < nodeConnectionCount; i++) {
            int from = buf.readInt();
            int to = buf.readInt();
            network.addConnection(new NodeConnection(from, to));
        }

        return network;
    }

    public void send() {
        ServerPlayNetworking.send(
                player,
                tmpPackets.GOGGLES_NETWORK,
                this.getBuffer()
        );
    }

    /**
     * Generates the packet buffer from the SimplifiedWiredNetwork.
     * <p>
     * The packet is formatted as follows:
     * 0. [String] Dimension
     * 1. [int] Node count
     * 2. [int] Peripheral count
     * 3. [int] Node connection count
     * 4. [int] Peripheral connection count
     * 5... [Nodes]
     * 6... [Peripherals]
     * 7... [Peripheral connections]
     * 8... [Node connections]
     * Where a node is the following:
     * 0. [Vec3i] (3x ints) Position
     * Where a peripheral is the following:
     * 0. [Vec3i] (3x ints) Position
     * 1. [Vec3i] (3x ints) Origin Position (the wired modem this peripheral connects to).
     * 2. [String] Name
     * Where a Node connection is the following:
     * 0. [int] from (node list)
     * 1. [int] to (node list)
     * Where a peripheral connection is just a node connection, but from is node list and to is peripheral list.
     *
     * Noting that peripheral connections are written before node connections since nodes need peripherals to instantiate themselves.
     *
     * @return The buffer to be sent.
     */
    private FriendlyByteBuf getBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();

        List<SimpleWiredNode> nodes = network.getNodes();
        List<NodeConnection> connections = network.getConnections();
        List<PeripheralNode> peripherals = network.getPeripherals();
        List<PeripheralConnection> peripheralConnections = network.getPeripheralConnections();

        // Dimension
        PacketHelper.writeString(buf, network.getDimension());

        // Node count
        buf.writeInt(nodes.size());
        // Peripheral count
        buf.writeInt(peripherals.size());
        // connections count
        buf.writeInt(connections.size());
        // peripheral connections count
        buf.writeInt(peripheralConnections.size());

        // Write nodes
        for (SimpleWiredNode node : nodes) {
            PacketHelper.writeVec3i(buf, node.position());
        }

        // Write peripherals
        for (PeripheralNode node : peripherals) {
            PacketHelper.writeVec3i(buf, node.position());
            PacketHelper.writeVec3i(buf, node.originPosition());
            PacketHelper.writeString(buf, node.name());
        }

        // Write peripheral connections
        for (PeripheralConnection peripheralConnection : peripheralConnections) {
            buf.writeInt(peripheralConnection.from());
            buf.writeInt(peripheralConnection.to());
        }

        // Write connections
        for (NodeConnection connection : connections) {
            buf.writeInt(connection.from());
            buf.writeInt(connection.to());
        }



        return buf;
    }
}
