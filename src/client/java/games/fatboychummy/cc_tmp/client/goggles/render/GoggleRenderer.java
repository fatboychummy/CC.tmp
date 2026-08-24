package games.fatboychummy.cc_tmp.client.goggles.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.cc.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GoggleRenderer {
    public static void init() {
        WorldRenderEvents.LAST.register(GoggleRenderer::renderGoggles);
        data = null;
        client = Minecraft.getInstance();
    }

    private static void renderGoggles(WorldRenderContext context) {
        PoseStack matrices = context.matrixStack();
        Camera camera = context.camera();

        if (camera == null) return;

        if (data != null) renderLookedAtPeripheral(context, matrices, camera);

        renderAllNetworks(context, matrices, camera);
    }

    public static void addNetwork(SimpleWiredNetwork network) {
        networks.add(network);
    }

    private static void renderLookedAtPeripheral(WorldRenderContext context, PoseStack matrices, Camera camera) {
        assert data != null;
    }

    private static void renderAllNetworks(WorldRenderContext context, PoseStack matrices, Camera camera) {
        // We will render in four passes
        // The first pass will just gather the *location* of all leaf nodes.
        //    This is so we can see how many network nodes arrive at a specific position for offsetting the render.
        // The second pass will then call `renderNetworkNodes` on each network.
        //    This pass renders the squares or spheres in the network.
        // The third pass will call `renderNetworkLines` on each network.
        // The fourth pass will call `renderNetworkNametags` on each network.

        Map<Vec3i, Integer> positionCounts = new HashMap<>();
        Map<Vec3i, List<Double>> finalOffsets = new HashMap<>();

        // First pass.
        for (SimpleWiredNetwork network : networks) {
            for (SimpleWiredNode node : network.getNodes()) {
                for (PeripheralNode peripheral : node.peripherals()) {
                    positionCounts.merge(peripheral.position(), 1, Integer::sum);
                }
            }
        }

        // Second pass
        for (SimpleWiredNetwork network : networks) {
            renderNetworkNodes(context, matrices, camera, network, positionCounts, finalOffsets);
        }

        // Third pass
        for (SimpleWiredNetwork network : networks) {
            renderNetworkConnections(context, matrices, camera, network, positionCounts, finalOffsets);
        }

        // Fourth pass
        for (SimpleWiredNetwork network : networks) {
            renderNetworkNametags(context, matrices, camera, network, positionCounts);
        }
    }

    private static void renderNetworkNodes(
            WorldRenderContext context,
            PoseStack matrices,
            Camera camera,
            SimpleWiredNetwork network,
            Map<Vec3i, Integer> positionCounts,
            Map<Vec3i, List<Double>> finalOffsets
    ) {
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.disableDepthTest();

        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_OVERLAY);

        int color = network.getColor();

        Map<Vec3i, Integer> seenPositions = new HashMap<>();

        for (PeripheralNode peripheral : network.getPeripherals()) {
            Vec3i pos = peripheral.position();
            Vec3 center = Vec3.atCenterOf(pos);
            Vec3 relative = center.subtract(cameraPos);
            if (positionCounts.get(pos) > 1) {
                int max = positionCounts.get(pos);
                int cur = seenPositions.getOrDefault(pos, 0);
                ++cur;

                final double MAX_CONTAINMENT = 0.5d;
                double offset = -MAX_CONTAINMENT / 2.0d
                        + MAX_CONTAINMENT * (cur - 1.0d) / (max - 1.0d);
                finalOffsets.computeIfAbsent(pos, (key) -> new ArrayList<>());
                finalOffsets.get(pos).add(offset);

                relative = new Vec3(relative.x, relative.y + offset, relative.z);
            }
            seenPositions.merge(pos, 1, Integer::sum);


            matrices.pushPose();
            matrices.translate(relative.x, relative.y, relative.z);

            matrices.scale(0.15f, 0.15f, 0.15f);
            matrices.mulPose(Axis.YP.rotation(45.0f));
            matrices.mulPose(Axis.XP.rotation(45.0f));
            matrices.mulPose(Axis.ZP.rotation(45.0f));

            overlayLeaf(consumer, matrices.last().pose(), color);

            matrices.popPose();
        }

        for (SimpleWiredNode node : network.getNodes()) {
            Vec3 center = Vec3.atCenterOf(node.position());
            Vec3 relative = center.subtract(cameraPos);

            matrices.pushPose();
            matrices.translate(relative.x, relative.y, relative.z);

            matrices.scale(0.15f, 0.15f, 0.15f);

            overlayNode(consumer, matrices.last().pose(), color);

            matrices.popPose();
        }

        buffer.endBatch(GoggleRenderTypes.GOGGLE_OVERLAY);
    }

    private static void renderNetworkConnections(
            WorldRenderContext context,
            PoseStack matrices,
            Camera camera,
            SimpleWiredNetwork network,
            Map<Vec3i, Integer> positionCounts,
            Map<Vec3i, List<Double>> finalOffsets
    ) {
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        //RenderSystem.disableDepthTest();

        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_OVERLAY);
        int color = network.getColor();
        Map<Vec3i, Integer> seenPositions = new HashMap<>();
        List<SimpleWiredNode> nodes = network.getNodes();
        List<PeripheralNode> peripherals = network.getPeripherals();

        for (NodeConnection connection : network.getConnections()) {
            Vec3i fromO = nodes.get(connection.from()).position();
            Vec3i toO = nodes.get(connection.to()).position();
            Vec3 from = Vec3.atCenterOf(fromO);
            Vec3 to = Vec3.atCenterOf(toO);

            Vec3 start = from.subtract(cameraPos);
            Vec3 end = to.subtract(cameraPos);

            Vec3 direction = to.subtract(from).normalize();
            Vec3 toCamera = cameraPos.subtract(from).normalize();
            Vec3 side = direction.cross(toCamera).normalize();
            side = side.scale(0.05);

            Vec3 a = start.add(side);
            Vec3 b = start.subtract(side);
            Vec3 c = end.subtract(side);
            Vec3 d = end.add(side);

            Matrix4f pose = matrices.last().pose();

            //Cc_tmp.LOGGER.info("RENDERING LINE {} TO {}", start, end);

            consumer.vertex(pose, (float) a.x, (float) a.y, (float) a.z)
                    .color(color)
                    .endVertex();
            consumer.vertex(pose, (float) b.x, (float) b.y, (float) b.z)
                    .color(color)
                    .endVertex();
            consumer.vertex(pose, (float) c.x, (float) c.y, (float) c.z)
                    .color(color)
                    .endVertex();
            consumer.vertex(pose, (float) d.x, (float) d.y, (float) d.z)
                    .color(color)
                    .endVertex();
        }

        // Peripheral connections
        for (PeripheralConnection connection : network.getPeripheralConnections()) {
            Vec3i fromO = nodes.get(connection.from()).position();
            Vec3i toO = peripherals.get(connection.to()).position();
            Vec3 from = Vec3.atCenterOf(fromO);
            Vec3 to = Vec3.atCenterOf(toO);

            Vec3 start = from.subtract(cameraPos);
            Vec3 end = to.subtract(cameraPos);
            if (positionCounts.get(toO) > 1) {
                int max = positionCounts.get(toO);
                int cur = seenPositions.getOrDefault(toO, 0);
                ++cur;

                final double MAX_CONTAINMENT = 0.5d;
                double offset = -MAX_CONTAINMENT / 2.0d
                        + MAX_CONTAINMENT * (cur - 1.0d) / (max - 1.0d);
                finalOffsets.computeIfAbsent(toO, (key) -> new ArrayList<>());
                finalOffsets.get(toO).add(offset);

                end = new Vec3(end.x, end.y + offset, end.z);
            }
            seenPositions.merge(toO, 1, Integer::sum);

            Vec3 direction = to.subtract(from).normalize();
            Vec3 toCamera = cameraPos.subtract(from).normalize();
            Vec3 side = direction.cross(toCamera).normalize();
            side = side.scale(0.05);

            Vec3 a = start.add(side);
            Vec3 b = start.subtract(side);
            Vec3 c = end.subtract(side);
            Vec3 d = end.add(side);

            Matrix4f pose = matrices.last().pose();

            //Cc_tmp.LOGGER.info("RENDERING PERIPHERAL LINE {} TO {}", start, end);

            consumer.vertex(pose, (float) a.x, (float) a.y, (float) a.z)
                    .color(color)
                    .endVertex();
            consumer.vertex(pose, (float) b.x, (float) b.y, (float) b.z)
                    .color(color)
                    .endVertex();
            consumer.vertex(pose, (float) c.x, (float) c.y, (float) c.z)
                    .color(color)
                    .endVertex();
            consumer.vertex(pose, (float) d.x, (float) d.y, (float) d.z)
                    .color(color)
                    .endVertex();
        }

        buffer.endBatch(GoggleRenderTypes.GOGGLE_OVERLAY);
    }

    private static void renderNetworkNametags(WorldRenderContext context, PoseStack matrices, Camera camera, SimpleWiredNetwork network, Map<Vec3i, Integer> positionCounts) {

    }

    // TODO: Needs to be dual pass, one pass renders the overlays, the other renders the labels.
    /*
    private static void renderOtherPeripherals(WorldRenderContext context, PoseStack matrices, Camera camera) {
        assert !dataList.isEmpty();
        Vec3 cameraPos = camera.getPosition();

        MultiBufferSource.BufferSource buffer =
                Minecraft.getInstance().renderBuffers().bufferSource();

        //MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(new BufferBuilder(256));

        RenderSystem.disableDepthTest();

        try {
            for (GogglePeripheralData peripheral : dataList) {
                Vec3 center = Vec3.atCenterOf(peripheral.pos());
                Vec3 relative = center.subtract(cameraPos);

                // Opacity level based on the FADE_START_DISTANCE and FADE_END_DISTANCE.
                float distance = (float) Math.sqrt(relative.lengthSqr());

                float fade = Mth.clamp(
                        (distance - FADE_START_DISTANCE)
                                / (FADE_END_DISTANCE - FADE_START_DISTANCE),
                        0.0f,
                        1.0f
                );
                float alpha = 1.0f - fade;

                if (alpha < 0.05f) {
                    continue;
                }
                alpha = Mth.clamp(alpha, 0.0f, 1.0f);

                int alphaByte = (int) (alpha * 255.0f);
                int color = (alphaByte << 24) | 0xd5d5d5;

                matrices.pushPose();
                matrices.translate(relative.x, relative.y, relative.z);
                overlayNode(context, matrices, buffer, new WiredNetwork()); // TODO: Update this temporary code
                buffer.endBatch(GoggleRenderTypes.GOGGLE_OVERLAY);

                matrices.translate(0.0f, 0.7f, 0.0f);

                // Render the label

                // Rotate towards camera.
                matrices.mulPose(camera.rotation());

                // Become smol
                float scale = 0.015f; // To be changed to a config?
                matrices.scale(-scale, -scale, scale);


                Component text = Component.literal(peripheral.name());
                client.font.drawInBatch(
                        text,
                        -client.font.width(text) / 2.0f,
                        0,
                        color,
                        false,
                        matrices.last().pose(),
                        buffer,
                        Font.DisplayMode.SEE_THROUGH,
                        0x0,
                        0xF000F0
                );
                matrices.popPose();
            }

            buffer.endBatch();
            //buffer.endBatch(GoggleRenderTypes.GOGGLE_OVERLAY);
            //buffer.endLastBatch();
        } finally {
            //buffer.endBatch();
            RenderSystem.enableDepthTest();
        }
    }
     */

    // Generates an overlay of a node (block shaped) at the current position.
    private static void overlayNode(VertexConsumer consumer, Matrix4f pose, int color) {
        // Top face (1Y)
        consumer.vertex(pose, -0.5f,  0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f,  0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f,  0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f,  0.5f,  0.5f)
                .color(color)
                .endVertex();

        // Bottom face (0Y)
        consumer.vertex(pose, -0.5f, -0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f, -0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f, -0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f, -0.5f,  0.5f)
                .color(color)
                .endVertex();

        // Front face (1Z)
        consumer.vertex(pose, -0.5f, -0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f, -0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f,  0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f,  0.5f,  0.5f)
                .color(color)
                .endVertex();

        // Left face (1X)
        consumer.vertex(pose,  0.5f, -0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f,  0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f,  0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f, -0.5f, -0.5f)
                .color(color)
                .endVertex();

        // Right face (0X)
        consumer.vertex(pose, -0.5f, -0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f, -0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f,  0.5f,  0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f,  0.5f, -0.5f)
                .color(color)
                .endVertex();

        // Back face (0Z)
        consumer.vertex(pose,  0.5f, -0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose,  0.5f,  0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f,  0.5f, -0.5f)
                .color(color)
                .endVertex();
        consumer.vertex(pose, -0.5f, -0.5f, -0.5f)
                .color(color)
                .endVertex();
    }

    // Generates an overlay of a leaf node (circle-shaped) at the current position.
    private static void overlayLeaf(VertexConsumer consumer, Matrix4f pose, int color) {
        // Nothing right now. Will eventually render a circle or something.
        overlayNode(consumer, pose, color);
    }

    // Connects two nodes on a network with a quad-line.
    private static void connectNodes(WorldRenderContext context, PoseStack matrices, MultiBufferSource buffer, SimpleWiredNetwork network, BlockPos pos1, BlockPos pos2) {}

    // Draws the nametag overlay for a networked block.
    private static void overlayName(WorldRenderContext context, PoseStack matrices, MultiBufferSource buffer, SimpleWiredNetwork network, BlockPos pos) {}

    static Minecraft client;

    // The data of all nearby networks, sent by the server.
    private static final List<SimpleWiredNetwork> networks = new CopyOnWriteArrayList<>();

    // The data of the currently looked-at object.
    private static @Nullable GogglePeripheralData data;

    private static final float FADE_START_DISTANCE = 10.0f;
    private static final float FADE_END_DISTANCE = 15.0f;

    private static final ResourceLocation FONT_TEXTURE =
            new ResourceLocation("minecraft", "textures/font/ascii.png");
}
