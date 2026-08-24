package games.fatboychummy.cc_tmp.mixin;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.impl.network.wired.WiredNodeImpl;
import it.unimi.dsi.fastutil.Hash;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(targets = "dan200.computercraft.impl.network.wired.WiredNodeImpl")
public interface WiredNodeImplAccessor {
    @Accessor("neighbours")
    HashSet<WiredNodeImpl> getNeighbours();

    @Accessor("peripherals")
    Map<String, IPeripheral> getPeripherals();
}
