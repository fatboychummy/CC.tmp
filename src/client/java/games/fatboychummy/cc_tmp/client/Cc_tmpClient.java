package games.fatboychummy.cc_tmp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.api.network.wired.WiredNetwork;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.block.tmpBlocks;
import games.fatboychummy.cc_tmp.client.goggles.GoggleNetworkPacketHandler;
import games.fatboychummy.cc_tmp.client.scanner.PeripheralDocResolver;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDocRegistry;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.ScanCompletePacketHandler;
import games.fatboychummy.cc_tmp.client.goggles.render.GoggleRenderer;
import games.fatboychummy.cc_tmp.item.PeripheralScannerItem;
import games.fatboychummy.cc_tmp.item.tmpItems;
import games.fatboychummy.cc_tmp.packet.tmpPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class Cc_tmpClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Cc_tmp.LOGGER.info("Initializing Too Many Peripherals Client");
        PeripheralDocResolver.init();
        GoggleRenderer.init();

        FabricModelPredicateProviderRegistry.register(
                tmpItems.PERIPHERAL_SCANNER,
                Cc_tmp.id("scanning"),
                (stack, level, entity, seed) -> stack.getOrCreateTag().getBoolean(PeripheralScannerItem.TAG_SCANNING) ? 1.0F : 0.0F
        );

        BlockRenderLayerMap.INSTANCE.putBlock(
                tmpBlocks.SCANNER_OVERLAY,
                RenderType.translucent()
        );

        ResourceManagerHelper.get(PackType.SERVER_DATA)
                        .registerReloadListener(new PeripheralDocRegistry());

        WorldRenderEvents.AFTER_TRANSLUCENT.register(Cc_tmpClient::animateScanner);

        ClientPlayNetworking.registerGlobalReceiver(
                tmpPackets.SCAN_COMPLETE,
                ScanCompletePacketHandler::listen
        );

        ClientPlayNetworking.registerGlobalReceiver(
                tmpPackets.GOGGLES_NETWORK,
                GoggleNetworkPacketHandler::listen
        );
    }

    private static void animateScanner(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        ItemStack stack = player.getUseItem();

        if (!(stack.getItem() instanceof PeripheralScannerItem scanner)) return;

        BlockPos pos = scanner.getScanPos(stack.getOrCreateTag()); // We could also check `.getScanning()`, but the scan pos only exists if scanning anyways.
        if (pos == null) return;

        PoseStack matrices = context.matrixStack();
        Vec3 camera = context.camera().getPosition();
        matrices.pushPose();

        matrices.translate(
                pos.getX() - camera.x,
                pos.getY() - camera.y,
                pos.getZ() - camera.z
        );

        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        dispatcher.renderSingleBlock(
                tmpBlocks.SCANNER_OVERLAY.defaultBlockState(),
                matrices,
                context.consumers(),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );

        matrices.popPose();
    }
}
