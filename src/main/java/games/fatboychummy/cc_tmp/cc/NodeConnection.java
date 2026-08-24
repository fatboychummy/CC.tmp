package games.fatboychummy.cc_tmp.cc;

import org.jetbrains.annotations.NotNull;

public record NodeConnection(
        int from,
        int to
) {
    public NodeConnection {
        if (from > to) {
            int temp = from;
            from = to;
            to = temp;
        }
    }

    public @NotNull String toString() {
        return from + " -> " + to;
    }
}
