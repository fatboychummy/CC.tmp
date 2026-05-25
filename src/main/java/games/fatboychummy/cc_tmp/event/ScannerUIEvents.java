package games.fatboychummy.cc_tmp.event;

import java.util.ArrayList;
import java.util.List;

public class ScannerUIEvents {
    private ScannerUIEvents() {}

    public interface OpenScannerListener {
        void open();
    }

    private static final List<OpenScannerListener> LISTENERS = new ArrayList<>();

    public static void register(OpenScannerListener listener) {
        LISTENERS.add(listener);
    }

    public static void openScannerRequested() {
        for (OpenScannerListener listener : LISTENERS) {
            listener.open();
        }
    }
}
