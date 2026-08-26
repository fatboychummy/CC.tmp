package games.fatboychummy.cc_tmp.cc;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public record PeripheralNode(Vec3i position, Vec3i originPosition, String name) {
    public static @Nullable PeripheralNode fromIPeripheral(String name, IPeripheral peripheral, Vec3i originPosition) {
        Object target = peripheral.getTarget();
        if (!(target instanceof BlockEntity blockEntity)) {
            return null;
        }
        assert target instanceof BlockEntity;
        BlockPos pos = blockEntity.getBlockPos();
        return new PeripheralNode(
                new Vec3i(pos.getX(), pos.getY(), pos.getZ()),
                originPosition,
                name
        );
    }
}
