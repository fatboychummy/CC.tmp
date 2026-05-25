package games.fatboychummy.cc_tmp.item;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dan200.computercraft.impl.Peripherals;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.event.ScannerUIEvents;
import games.fatboychummy.cc_tmp.packet.PacketHelper;
import games.fatboychummy.cc_tmp.packet.tmpPackets;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class PeripheralScannerItem extends Item {
    public static final String ID = "peripheral_scanner";
    public static final String TAG_SCANNING = "Scanning";
    public static final String TAG_SCAN_COMPLETION = "ScanCompletion";
    public static final String TAG_SCAN_POS_X = "ScanPosX";
    public static final String TAG_SCAN_POS_Y = "ScanPosY";
    public static final String TAG_SCAN_POS_Z = "ScanPosZ";
    private static final int scanDuration = 60;

    public PeripheralScannerItem(Properties properties) {
        super(properties);
    }

    private void setScanPos(@NotNull CompoundTag tag, @Nullable BlockPos pos) {
        if (pos == null) {
            tag.remove(TAG_SCAN_POS_X);
            tag.remove(TAG_SCAN_POS_Y);
            tag.remove(TAG_SCAN_POS_Z);
            return;
        }

        tag.putInt(TAG_SCAN_POS_X, pos.getX());
        tag.putInt(TAG_SCAN_POS_Y, pos.getY());
        tag.putInt(TAG_SCAN_POS_Z, pos.getZ());
    }

    public @Nullable BlockPos getScanPos(@NotNull CompoundTag tag) {
        if (!tag.contains(TAG_SCAN_POS_X)) return null;

        return new BlockPos(
                tag.getInt(TAG_SCAN_POS_X),
                tag.getInt(TAG_SCAN_POS_Y),
                tag.getInt(TAG_SCAN_POS_Z)
        );
    }

    private void setScanCompletion(@NotNull CompoundTag tag, int completion) {
        tag.putInt(TAG_SCAN_COMPLETION, completion);
    }

    public int getScanCompletion(@NotNull CompoundTag tag) {
        return tag.getInt(TAG_SCAN_COMPLETION);
    }

    private void setScanning(@NotNull CompoundTag tag, boolean scan) {
        tag.putBoolean(TAG_SCANNING, scan);
    }

    public boolean getScanning(@NotNull CompoundTag tag) {
        return tag.getBoolean(TAG_SCANNING);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        CompoundTag tag = itemStack.getOrCreateTag();

        if (!getScanning(tag)) {
            player.startUsingItem(interactionHand);
            setScanning(tag, true);
        }

        setScanCompletion(tag, 0);
        setScanPos(tag, null);

        return InteractionResultHolder.consume(itemStack);
    }

    private void onScanCompletion(Level level, Player player, ItemStack itemStack) {
        BlockPos scanPos = getScanPos(itemStack.getOrCreateTag());
        if (scanPos == null) {
            Cc_tmp.LOGGER.warn("Can't find scan pos for peripheral_scanner after scan completion event.");
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    scanPos.getX() + 0.5,
                    scanPos.getY() + 0.5,
                    scanPos.getZ() + 0.5,
                    40,
                    0.5,
                    0.5,
                    0.5,
                    0.1
            );
            level.playSound(
                    null,
                    scanPos,
                    SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.5F
            );

            // Notify the player to run the screen.

            // Get all information needed from the level.
            BlockState state = serverLevel.getBlockState(scanPos);
            Block block = state.getBlock();
            BlockEntity be = serverLevel.getBlockEntity(scanPos);
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            String modId = id.getNamespace();

            // Get the main peripheral provider.
            BlockApiLookup.BlockApiProvider<IPeripheral, Direction> provider = PeripheralLookup.get().getProvider(block);

            // Then, check if a generic peripheral exists for this peripheral.
            IPeripheral generic = Peripherals.getGenericPeripheral(serverLevel, scanPos, Direction.UP, be, null);

            // If neither exist, fail.
            if (provider == null && generic == null) {
                Cc_tmp.LOGGER.warn("No provider or generic for {}", block.getName().getString());
                return;
            }

            // Peripherals have two type systems
            // First is the main `getType`, from olden days of CC:T where only one type existed.
            // Then there is `getAdditionalTypes`.
            // We need to combine them, but only do it for whatever peripheral type we have.
            String mainPeripheralType;
            Set<String> additionalPeripheralTypes;
            if (generic != null) { // We check generic peripheral first since we can pass if provider is null if generic is not.
                mainPeripheralType = generic.getType();
                additionalPeripheralTypes = generic.getAdditionalTypes();
            } else {
                // If it's not a generic peripheral, check for a peripheral at the given position
                // I have no clue what the 'context' value (the last null) is supposed to be.
                IPeripheral peripheral = provider.find(serverLevel, scanPos, state, be, null);
                if (peripheral == null) { // No peripheral, fail.
                    Cc_tmp.LOGGER.warn("No peripheral for {}", block.getName().getString());
                    return;
                }
                // Get the data.
                mainPeripheralType = peripheral.getType();
                additionalPeripheralTypes = peripheral.getAdditionalTypes();
            }

            FriendlyByteBuf buf = PacketByteBufs.create();
            PacketHelper.writeString(buf, modId);
            buf.writeInt(additionalPeripheralTypes.size() + 1);
            PacketHelper.writeString(buf, mainPeripheralType);
            for (String additionalPeripheralType : additionalPeripheralTypes) {
                PacketHelper.writeString(buf, additionalPeripheralType);
            }

            ServerPlayNetworking.send(
                    (ServerPlayer) player,
                    tmpPackets.SCAN_COMPLETE,
                    buf
            );
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        if (!(livingEntity instanceof Player player)) return;

        // Raycast
        HitResult hit = player.pick(10.0D, 0.0F, false);
        CompoundTag tag = itemStack.getOrCreateTag();

        // If we hit an entity or nothing, stop.
        if (!(hit instanceof BlockHitResult blockHit)) {
            setScanCompletion(tag, 0);
            setScanPos(tag, null);
            return;
        }

        BlockPos scanPos = getScanPos(tag);

        // If we hit a block, compare our current hit position to the returned hit position
        if (!blockHit.getBlockPos().equals(scanPos)) {
            setScanCompletion(tag, 0);
            setScanPos(tag, blockHit.getBlockPos());
            return;
        }

        // Increment scan timer, check if we've scanned the block long enough.
        int completion = getScanCompletion(tag) + 1;
        setScanCompletion(tag, completion);
        if (completion >= scanDuration) {
            // TODO: Get peripheral info about the block we're looking at, and display it.
            onScanCompletion(level, player, itemStack);

            player.getCooldowns().addCooldown(this, scanDuration);
            player.stopUsingItem();
            releaseUsing(itemStack, level, player, i);
        }
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        CompoundTag tag = itemStack.getOrCreateTag();
        setScanning(tag, false);
        setScanCompletion(tag, 0);
        setScanPos(tag, null);
    }

    @Override
    @NotNull
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }
}
