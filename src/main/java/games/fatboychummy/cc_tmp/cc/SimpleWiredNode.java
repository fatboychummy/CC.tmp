package games.fatboychummy.cc_tmp.cc;

import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SimpleWiredNode(
        Vec3i position,
        List<PeripheralNode> peripherals
) {
    public void addPeripheral(PeripheralNode node) {
        peripherals.add(node);
    }

    public @NotNull String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("SimplifiedWiredNode @ ");
        builder.append(position);
        builder.append(" [");

        for (int i = 0; i < peripherals.size(); i++) {
            PeripheralNode peripheral = peripherals.get(i);
            builder.append(peripheral.toString());
            if (i < peripherals.size() - 1) {
                builder.append(", ");
            }
        }

        builder.append("]");

        return builder.toString();
    }
}
