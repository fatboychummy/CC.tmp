package games.fatboychummy.cc_tmp.cc;

import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.network.wired.WiredNetwork;
import dan200.computercraft.api.network.wired.WiredNode;
import dan200.computercraft.api.peripheral.IPeripheral;import dan200.computercraft.impl.network.wired.WiredNodeImpl;
import games.fatboychummy.cc_tmp.mixin.WiredNetworkImplAccessor;
import games.fatboychummy.cc_tmp.mixin.WiredNodeImplAccessor;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;import org.jetbrains.annotations.NotNull;

import java.util.*;import java.util.concurrent.locks.ReadWriteLock;

// Contains all the data required to represent a single wired network. This data is broadcast by the server.
public class SimpleWiredNetwork {
    // A (hopefully unique) colour to display the network as.
    private final int color;
    private final List<SimpleWiredNode> nodes = new ArrayList<>();
    private final List<NodeConnection> connections = new ArrayList<>();
    private final List<PeripheralNode> peripherals = new ArrayList<>();
    private final List<PeripheralConnection> peripheralConnections = new ArrayList<>();
    private final Map<Integer, Integer> connectionMap = new HashMap<>();
    private final String dimension;

    public SimpleWiredNetwork(int color, String dimension) {
        this.color = color;
        this.dimension = dimension;
    }

    public void addNode(SimpleWiredNode node) {
        nodes.add(node);
        for (PeripheralNode peripheral : node.peripherals()) {
            peripherals.add(peripheral);
            peripheralConnections.add(new PeripheralConnection(nodes.indexOf(node), peripherals.indexOf(peripheral)));
        }
    }

    public void addConnection(NodeConnection connection) {
        connections.add(connection);
        connectionMap.put(connection.from(), connection.to());
    }

    public Map<Integer, Integer> getConnectionMap() {
        return connectionMap;
    }

    public final List<SimpleWiredNode> getNodes() {
        return nodes;
    }

    public final List<NodeConnection> getConnections() {
        return connections;
    }

    public final List<PeripheralNode> getPeripherals() {
        return peripherals;
    }

    public final  List<PeripheralConnection> getPeripheralConnections() {
        return peripheralConnections;
    }

    public final int getColor() {
        return color;
    }

    public final String getDimension() {
        return dimension;
    }

    public final @NotNull String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("SimplifiedWiredNetwork {");
        builder.append("||| Nodes");
        for (int i = 0; i < nodes.size(); i++) {
            SimpleWiredNode node = nodes.get(i);
            builder.append("\n  ")
                            .append(i)
                            .append(": ");
            builder.append(node.toString());
            if (i < nodes.size() - 1) builder.append(", ");
            else builder.append("\n");
        }
        builder.append("||| Connections");
        for (int i = 0; i < connections.size(); i++) {
            NodeConnection connection = connections.get(i);
            builder.append("\n  ")
                            .append(i)
                            .append(": ");
            builder.append(connection.toString());
            if (i < connections.size() - 1) builder.append(", ");
            else builder.append("\n");
        }
        builder.append("||| Peripherals");
        for (int i = 0; i < peripherals.size(); i++) {
            PeripheralNode node = peripherals.get(i);
            builder.append("\n  ")
                    .append(i)
                    .append(": ");
            builder.append(node.toString());
            if (i < peripherals.size() - 1) builder.append(", ");
            else builder.append("\n");
        }
        builder.append("||| PeripheralConnections");
        for (int i = 0; i < peripheralConnections.size(); i++) {
            PeripheralConnection connection = peripheralConnections.get(i);
            builder.append("\n  ")
                    .append(i)
                    .append(": ");
            builder.append(connection.toString());
            if (i < peripheralConnections.size() - 1) builder.append(", ");
            else builder.append("\n");
        }
        builder.append("}");

        return builder.toString();
    }

    // I could just use Math.ceil directly, but like... What if I'm rounding wrong?
    // AHA, I KNEW IT
    // I think I need math.floor
    private static int round(double n) {
        return (int) Math.floor(n);
    }

    public static SimpleWiredNetwork computeNetwork(@NotNull String dimension, @NotNull WiredNetwork network) {
        WiredNetworkImplAccessor WNetAccessor = (WiredNetworkImplAccessor) network;
        Set<WiredNodeImpl> nodes = WNetAccessor.getNodes();
        ArrayList<SimpleWiredNode> nodesList = new ArrayList<>();
        Set<NodeConnection> connectionSet = new HashSet<>();
        Map<WiredNode, Integer> lookup = new HashMap<>();

        ReadWriteLock lock = WNetAccessor.getLock();
        lock.readLock().lock();
        try {
            // Pass one: Save nodes
            int i = 0;
            for (WiredNode node : nodes) {
                WiredElement element = node.getElement();
                Vec3 elementPos = element.getPosition();
                Vec3i pos = new Vec3i(round(elementPos.x), round(elementPos.y), round(elementPos.z));
                ArrayList<PeripheralNode> nodePeripherals = new ArrayList<>();
                Map<String, IPeripheral> peripherals = ((WiredNodeImplAccessor) node).getPeripherals();

                for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
                    nodePeripherals.add(PeripheralNode.fromIPeripheral(entry.getKey(), entry.getValue(), pos));
                }



                nodesList.add(
                        new SimpleWiredNode(
                                new Vec3i(
                                        round(elementPos.x()),
                                        round(elementPos.y()),
                                        round(elementPos.z())
                                ),
                                nodePeripherals
                        )
                );
                lookup.put(node, i);
                i++;
            }

            // Pass two: Save connections
            for (WiredNode node : nodes) {
                int id = lookup.get(node); // Find OUR id.
                Set<WiredNodeImpl> neighbours = ((WiredNodeImplAccessor) node).getNeighbours();
                for (WiredNodeImpl neighbour : neighbours) {
                    // SimplifiedWiredConnection normalizes the connections, so despite there being duplicate connections
                    // (one going one way, one going the other), this will not result in duplicates.
                    connectionSet.add(new NodeConnection(
                            id,
                            lookup.get(neighbour) // Find NEIGHBOUR id.
                    ));
                }
            }

            // Pass two point five: Save peripheral connections
            for (WiredNode node : nodes) {
                int id = lookup.get(node);
            }
        } finally {
            lock.readLock().unlock();
        }


        // Final setup: Create the network object.
        SimpleWiredNetwork sNetwork = new SimpleWiredNetwork(0x5500ff00, dimension);

        for (SimpleWiredNode node : nodesList) {
            sNetwork.addNode(node);
        }

        for (NodeConnection connection : connectionSet) {
            sNetwork.addConnection(connection);
        }

        return sNetwork;
    }
}
