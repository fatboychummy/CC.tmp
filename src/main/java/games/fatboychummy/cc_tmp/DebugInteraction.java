package games.fatboychummy.cc_tmp;

import dan200.computercraft.api.network.PacketSender;import dan200.computercraft.api.network.wired.WiredElement;
import dan200.computercraft.api.network.wired.WiredNetwork;
import dan200.computercraft.api.network.wired.WiredNode;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.impl.network.wired.WiredNodeImpl;
import dan200.computercraft.shared.peripheral.modem.wired.CableBlockEntity;
import dan200.computercraft.shared.peripheral.modem.wired.WiredModemFullBlockEntity;
import games.fatboychummy.cc_tmp.mixin.WiredNetworkImplAccessor;
import games.fatboychummy.cc_tmp.mixin.WiredNodeImplAccessor;
import games.fatboychummy.cc_tmp.packet.GoggleNetworkPacket;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DebugInteraction {
    public static void init() {
        UseBlockCallback.EVENT.register(DebugInteraction::interaction);
    }

    private static InteractionResult interaction(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (!enabled) return InteractionResult.PASS;
        // Only allow server-side running of this method.
        if (!(world instanceof ServerLevel)) return InteractionResult.PASS;
        assert player instanceof ServerPlayer;
        if (!player.isCrouching()) return InteractionResult.PASS;
        ServerPlayer serverPlayer = ((ServerPlayer) player);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();

        BlockEntity entity = world.getBlockEntity(pos);


        if (!(entity instanceof CableBlockEntity) && !(entity instanceof WiredModemFullBlockEntity)) {
            return InteractionResult.PASS;
        }

        textOutput(entity);

        transmit(serverPlayer, entity);

        return InteractionResult.PASS;
    }

    private static void transmit(ServerPlayer player, BlockEntity entity) {
        WiredNetwork network;
        WiredNode node;
        if (entity instanceof WiredModemFullBlockEntity) {
            // entity.getElement()
            WiredElement element = ((WiredModemFullBlockEntity) entity).getElement();
            node = element.getNode();
            network = node.getNetwork();
        } else {
            // CableBlockEntity
            // entity.getWiredElement()
            // Do we want to worry about cables right now?
            return;
        }

        new GoggleNetworkPacket(player, node).send();
    }

    private static void textOutput(BlockEntity entity) {
        WiredNetwork network;
        WiredNode node;
        if (entity instanceof WiredModemFullBlockEntity) {
            // entity.getElement()
            WiredElement element = ((WiredModemFullBlockEntity) entity).getElement();
            node = element.getNode();
            network = node.getNetwork();
        } else {
            // CableBlockEntity
            // entity.getWiredElement()
            // Do we want to worry about cables right now?
            return;
        }

        Set<WiredNodeImpl> nodes = ((WiredNetworkImplAccessor) network).getNodes();

        Cc_tmp.LOGGER.info("NODES:");
        for (WiredNodeImpl _node : nodes) {
            WiredElement _element = _node.getElement();
            Cc_tmp.LOGGER.info("  - {} @ {}", _element.getSenderID(), _element.getPosition());
        }

        Cc_tmp.LOGGER.info("");
        Cc_tmp.LOGGER.info("Neighbours:");
        HashSet<WiredNodeImpl> neighbours = ((WiredNodeImplAccessor) node).getNeighbours();
        for (WiredNodeImpl neighbour : neighbours) {
            WiredElement _element = neighbour.getElement();
            Cc_tmp.LOGGER.info("  - {} @ {}", _element.getSenderID(), _element.getPosition());
        }

        Cc_tmp.LOGGER.info("");
        Cc_tmp.LOGGER.info("Peripherals:");
        Map<String, IPeripheral> peripherals = ((WiredNodeImplAccessor) node).getPeripherals();
        for (Map.Entry<String, IPeripheral> entry : peripherals.entrySet()) {
            if (entry.getValue().getTarget() instanceof BlockEntity) {
                Cc_tmp.LOGGER.info("  - {} @ {}", entry.getKey(), ((BlockEntity) entry.getValue().getTarget()).getBlockPos());
            } else {
                Cc_tmp.LOGGER.info("  - {} : {}", entry.getKey(), entry.getValue().getTarget());
            }
        }
    }

    public static boolean enabled = true;
}
