package games.fatboychummy.cc_tmp.cc;

import org.jetbrains.annotations.NotNull;

// Same as NodeConnection, but does not normalize!
public record PeripheralConnection(
        int from,
        int to
) {
    public @NotNull String toString() {
        return from + " -> " + to;
    }
}
