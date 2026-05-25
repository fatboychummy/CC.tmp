package games.fatboychummy.cc_tmp.client.scanner;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDoc;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDocRegistry;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralKey;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

public class PeripheralDocResolver {
    private static final Map<String, Version> modVersions = new HashMap<>();

    public static void init() {
        Cc_tmp.LOGGER.info("- Initializing PeripheralDocResolver");
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
             String id = mod.getMetadata().getId();
             Version version = mod.getMetadata().getVersion();

             modVersions.put(id, version);
        }
    }

    public static String[] getMethodNames(IPeripheral peripheral) {
        // TODO: Should we move this to use our PeripheralDocs instead?
        // Pros: Easier, less missed information.
        // Cons: If peripheral updates and adds/removes methods, we miss information (lol).
        if (peripheral instanceof IDynamicPeripheral dynamicPeripheral) {
            return dynamicPeripheral.getMethodNames();
        }

        // Reflect on the class, find any `@LuaFunction` annotations, and return the method names.
        ArrayList<String> methodNames = new ArrayList<>();

        for (Method m : peripheral.getClass().getMethods()) {
            LuaFunction lua = m.getAnnotation(LuaFunction.class);
            String methodName = m.getName();
            String[] valueNames = lua.value();

            if (valueNames.length == 0) valueNames = new String[]{methodName};

            methodNames.addAll(Arrays.asList(valueNames));
        }

        return methodNames.toArray(new String[0]);
    }

    public static Set<String> getPeripheralTypes(IPeripheral peripheral) {
        Set<String> result = new HashSet<>(peripheral.getAdditionalTypes());
        result.add(peripheral.getType());

        return result;
    }

    public static @Nullable PeripheralDoc[] getPeripheralDocs(String modId, String[] peripheralTypes) {
        ArrayList<PeripheralDoc> docs = new ArrayList<>();

        for (String type : peripheralTypes) {
            PeripheralDoc doc = PeripheralDocRegistry.get(
                    new PeripheralKey(modId, type),
                    modVersions.get(modId)
            );
            if (doc != null) {
                docs.add(doc);
            }
        }

        return docs.toArray(PeripheralDoc[]::new);
    }
}
