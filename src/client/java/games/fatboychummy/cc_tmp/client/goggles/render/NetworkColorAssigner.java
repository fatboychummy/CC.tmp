package games.fatboychummy.cc_tmp.client.goggles.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NetworkColorAssigner {
    public static int getNextColor(List<Integer> usedColors) {
        List<Color> availableColors = Color.getColors();
        availableColors.removeIf(color -> usedColors.contains(color.value));

        if (availableColors.isEmpty()) {
            // Reset it, just start from the beginning again.
            // FIXME: Every single call after the "buffer" is full will completely reset to a full list
            // If there are two greens but only one of every other color, green can be picked a third time!
            // We should probably keep some kind of instanced state in GoggleRenderer for both speed and so we can fix this.
            availableColors = Color.getColors();
        }

        return availableColors.get(
                ThreadLocalRandom.current().nextInt(0, availableColors.size())
        ).value;
    }

    public static enum Color {
        WHITE     (0xf0f0f0),
        ORANGE    (0xf2b233),
        MAGENTA   (0xe57fd8),
        LIGHT_BLUE(0x99b2f2),
        YELLOW    (0xdede6c),
        LIME      (0x7fcc19),
        PINK      (0xf2b2cc),
        GRAY      (0x4c4c4c),
        LIGHT_GRAY(0x999999),
        CYAN      (0x4c99b2),
        PURPLE    (0xb266e5),
        BLUE      (0x3366cc),
        BROWN     (0x7f664c),
        GREEN     (0x57a64e),
        RED       (0xcc4c4c),
        BLACK     (0x111111);

        public static List<Color> getColors() {
            return new ArrayList<>(Arrays.asList(values()));
        }

        private final int value;
        Color(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public int withAlpha(int alpha) {
            return alpha << 24 | value;
        }
    }
}
