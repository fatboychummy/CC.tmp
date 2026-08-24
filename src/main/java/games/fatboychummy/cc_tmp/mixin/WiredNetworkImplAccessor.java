package games.fatboychummy.cc_tmp.mixin;

import dan200.computercraft.impl.network.wired.WiredNodeImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

@Mixin(targets = "dan200.computercraft.impl.network.wired.WiredNetworkImpl")
public interface WiredNetworkImplAccessor {
    @Accessor("nodes")
    Set<WiredNodeImpl> getNodes();

    @Accessor("lock")
    ReadWriteLock getLock();
}
