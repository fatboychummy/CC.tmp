package games.fatboychummy.cc_tmp.client.scanner.docs;

import com.google.gson.JsonObject;
import games.fatboychummy.cc_tmp.Cc_tmp;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class PeripheralDocRegistry implements SimpleSynchronousResourceReloadListener {
    private static final Map<PeripheralKey, NavigableMap<Version, PeripheralDoc>> registry = new HashMap<>();


    public static @Nullable PeripheralDoc get(@NotNull PeripheralKey key, @NotNull Version version) {
        // Handle generic peripherals.
        Cc_tmp.LOGGER.info("Get: '{}'&'{}' -> '{}'", key.modID(), key.peripheralType(), version);
        String peripheralName = key.peripheralType();
        if (peripheralName.equals("energy_storage") || peripheralName.equals("fluid_storage") || peripheralName.equals("inventory")) {
            // Return the generic peripheral.
            Cc_tmp.LOGGER.info("Generic! Get: 'computercraft'&'{}'", peripheralName);
            NavigableMap<Version, PeripheralDoc> map = registry.get(new PeripheralKey(
                    "computercraft",
                    peripheralName
            ));

            if (map == null) {
                Cc_tmp.LOGGER.error("Map is null for generic peripheral {}!", peripheralName);
                return null;
            }

            Map.Entry<Version, PeripheralDoc> lowerMatch = map.floorEntry(version);
            Map.Entry<Version, PeripheralDoc> higherMatch = map.ceilingEntry(version);
            if (lowerMatch != null) {
                return lowerMatch.getValue();
            } else if (higherMatch != null) {
                return higherMatch.getValue();
            }
            return null;
        }


        NavigableMap<Version, PeripheralDoc> map = registry.get(key);

        if (map == null) {
            return null;
        }

        Map.Entry<Version, PeripheralDoc> lowerMatch = map.floorEntry(version);
        Map.Entry<Version, PeripheralDoc> higherMatch = map.ceilingEntry(version);

        return lowerMatch != null ? lowerMatch.getValue() : higherMatch != null ? higherMatch.getValue() : null;
    }

    private static void putDocument(@NotNull PeripheralDoc doc) throws VersionParsingException {
        PeripheralKey key = new PeripheralKey(doc.getModID(), doc.getPeripheralType());
        Version version = Version.parse(doc.getModVersion());

        registry.computeIfAbsent(key, k -> new TreeMap<>())
                .put(version, doc);
        Cc_tmp.LOGGER.info("{}&{} -> {}", key.modID(), key.peripheralType(), version);
    }

    private static void loadPeripheralDocument(ResourceLocation location, Resource resource) {
        Cc_tmp.LOGGER.info("Loading peripheral document: {}", location);
        try (
                InputStream stream = resource.open();
                Reader reader = new InputStreamReader(stream)
        ) {
            JsonObject json = GsonHelper.parse(reader);
            PeripheralDoc doc = PeripheralDoc.fromJson(json);
            putDocument(doc);
        } catch (Exception e) {
            Cc_tmp.LOGGER.error("Failed to load peripheral document '{}': {}", location, e);
            return;
        }
    }

    private String getFileName(ResourceLocation location) {
        String path = location.getPath();
        int slash = path.lastIndexOf('/');
        String name = (slash == -1) ? path : path.substring(slash + 1);

        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }

        return name;
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(Cc_tmp.MOD_ID, "cc_tmp/peripheral_docs");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        registry.clear();
        Cc_tmp.LOGGER.info("Reloading peripheral document registry");

        Map<ResourceLocation, Resource> resources = manager.listResources(
                "peripheral_docs",
                id -> id.getPath().endsWith(".json")
        );

        Cc_tmp.LOGGER.info("Got {} peripheral docs", resources.size());

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            loadPeripheralDocument(entry.getKey(), entry.getValue());
        }
        Cc_tmp.LOGGER.info("Loaded {} peripheral docs and {} peripheral methods", registry.size(), PeripheralDoc.loadedMethods);

        Cc_tmp.LOGGER.info("Done loading peripheral document registry");
    }
}
