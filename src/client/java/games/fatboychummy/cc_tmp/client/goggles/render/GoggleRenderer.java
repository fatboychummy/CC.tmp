package games.fatboychummy.cc_tmp.client.goggles.render;

import com.mojang.blaze3d.vertex.*;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.cc.*;
import games.fatboychummy.cc_tmp.packet.GoggleNetworkPacket;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
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

    public static int getNodeCount() {
        return nodeCount;
    }
    public static int getLeafCount() {
        return leafCount;
    }
    public static int getLineCount() {
        return lineCount;
    }
    public static int getNetworkCount() {
        return networks.size();
    }

    private static int nodeCount = 0;
    private static int leafCount = 0;
    private static int lineCount = 0;
    private static boolean dirty = false;

    private static void setupNetworks() {
        if (!dirty) return;

        nodeCount = 0;
        leafCount = 0;
        lineCount = 0;

        List<Integer> usedColors = new ArrayList<>();

        // Collection pass
        for (SimpleWiredNetwork network : networks) {
            nodeCount += network.getNodes().size();
            leafCount += network.getPeripherals().size();
            lineCount += network.getConnections().size();
            lineCount += network.getPeripheralConnections().size();

            int color = network.getColor();
            if (color == GoggleNetworkPacket.COLOR_NOT_INIT) continue;

            usedColors.add(color);
        }

        // Init pass
        for (SimpleWiredNetwork network : networks) {
            if (network.getColor() != GoggleNetworkPacket.COLOR_NOT_INIT) continue;

            int color = NetworkColorAssigner.getNextColor(usedColors);
            usedColors.add(color);
            network.setColor(color);
        }

        dirty = false;
    }

    private static void renderGoggles(WorldRenderContext context) {
        PoseStack matrices = context.matrixStack();
        Camera camera = context.camera();

        setupNetworks();

        if (camera == null) return;

        if (data != null) renderLookedAtPeripheral(context, matrices, camera);

        renderAllNetworks(context, matrices, camera);
    }

    public static void addNetwork(SimpleWiredNetwork network) {
        networks.add(network);
        dirty = true;
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

        // Second pass
        for (SimpleWiredNetwork network : networks) {
            renderNetworkNodes(matrices, camera, network);
        }
        for (SimpleWiredNetwork network : networks) {
            renderNetworkLeaves(matrices, camera, network);
        }

        // Third pass
        for (SimpleWiredNetwork network : networks) {
            renderNetworkConnections(matrices, camera, network);
        }

        // Fourth pass
        for (SimpleWiredNetwork network : networks) {
            renderNetworkNametags(matrices, camera, network);
        }
    }

    private static void renderNetworkLeaves(
            PoseStack matrices,
            Camera camera,
            SimpleWiredNetwork network
    ) {
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_TRIS);
        int color = network.getColor();

        for (PeripheralNode peripheral : network.getPeripherals()) {
            Vec3 from = Vec3.atCenterOf(peripheral.originPosition());
            Vec3 to = Vec3.atCenterOf(peripheral.position());
            Vec3 center = getLeafRenderPosition(from, to);
            Vec3 relative = center.subtract(cameraPos);

            matrices.pushPose();
            matrices.translate(relative.x, relative.y, relative.z);

            matrices.scale(0.15f, 0.15f, 0.15f);

            overlayLeaf(consumer, camera, matrices, color);

            matrices.popPose();
        }

        buffer.endBatch(GoggleRenderTypes.GOGGLE_TRIS);
    }

    private static void renderNetworkNodes(
            PoseStack matrices,
            Camera camera,
            SimpleWiredNetwork network
    ) {
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_QUADS);
        int color = network.getColor();

        for (SimpleWiredNode node : network.getNodes()) {
            Vec3 center = Vec3.atCenterOf(node.position());
            Vec3 relative = center.subtract(cameraPos);

            matrices.pushPose();
            matrices.translate(relative.x, relative.y, relative.z);

            matrices.scale(0.10f, 0.10f, 0.10f);

            overlayNode(consumer, matrices.last().pose(), color);

            matrices.popPose();
        }

        buffer.endBatch(GoggleRenderTypes.GOGGLE_QUADS);
    }

    private static void renderConnection(
            VertexConsumer consumer,
            Matrix4f pose,
            Vec3 cameraPos,
            Vec3 from,
            Vec3 to,
            int color
    ) {
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

    private static final float LEAF_OFFSET = 0.25f;
    private static Vec3 getLeafRenderPosition(
            Vec3 from,
            Vec3 to
    ) {
        Vec3 direction = from
                .subtract(to)
                .normalize();

        Vec3 offset = direction.scale(LEAF_OFFSET);
        return to.add(offset);
    }

    private static void renderNetworkConnections(
            PoseStack matrices,
            Camera camera,
            SimpleWiredNetwork network
    ) {
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_QUADS);
        int color = network.getColor();
        List<SimpleWiredNode> nodes = network.getNodes();
        List<PeripheralNode> peripherals = network.getPeripherals();

        Matrix4f pose = matrices.last().pose();
        for (NodeConnection connection : network.getConnections()) {
            Vec3 from = Vec3.atCenterOf(nodes.get(connection.from()).position());
            Vec3 to = Vec3.atCenterOf(nodes.get(connection.to()).position());

            renderConnection(
                    consumer,
                    pose,
                    cameraPos,
                    from,
                    to,
                    color
            );
        }

        // Peripheral connections

        for (PeripheralConnection connection : network.getPeripheralConnections()) {
            Vec3 from = Vec3.atCenterOf(nodes.get(connection.from()).position());
            Vec3 to = Vec3.atCenterOf(peripherals.get(connection.to()).position());

            renderConnection(
                    consumer,
                    pose,
                    cameraPos,
                    from,
                    getLeafRenderPosition(from, to),
                    color
            );
        }

        buffer.endBatch(GoggleRenderTypes.GOGGLE_QUADS);
    }

    private static void renderNetworkNametags(PoseStack matrices, Camera camera, SimpleWiredNetwork network) {

    }

    // Generates an overlay of a node (block shaped) at the current position.
    private static void overlayNode(VertexConsumer consumer, Matrix4f pose, int color) {
        // No way do I have these vertices correctly ordered, so I just disabled culling.
        // :)

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

    private static final int SEGMENTS = 4;
    private static void renderCircle(
            VertexConsumer consumer,
            Matrix4f pose,
            int color,
            float radius
    ) {
        for (int i = 0; i < SEGMENTS; i++) {
            double a1 = 2.0 * Math.PI * i / SEGMENTS;
            double a2 = 2.0 * Math.PI * (i + 1) / SEGMENTS;

            float x1 = (float) (Math.cos(a1) * radius);
            float y1 = (float) (Math.sin(a1) * radius);

            float x2 = (float) (Math.cos(a2) * radius);
            float y2 = (float) (Math.sin(a2) * radius);

            // Triangle
            consumer.vertex(pose, 0, 0, 0)
                    .color(color)
                    .endVertex();

            consumer.vertex(pose, x1, y1, 0)
                    .color(color)
                    .endVertex();

            consumer.vertex(pose, x2, y2, 0)
                    .color(color)
                    .endVertex();
        }
    }


    private static final int white = NetworkColorAssigner.Color.WHITE.withAlpha(0xff);
    // Generates an overlay of a leaf node (circle-shaped with smaller circle) at the current position.
    private static void overlayLeaf(VertexConsumer consumer, Camera camera, PoseStack matrices, int color) {
        // Nothing right now. Will eventually render a circle or something.
        //overlayNode(consumer, pose, color);

        matrices.pushPose();
        matrices.mulPose(camera.rotation());
        Matrix4f pose = matrices.last().pose();

        renderCircle(consumer, pose, color, 1.0f);

        // Render a smaller circle on top.
        renderCircle(consumer, pose, white, 0.5f);

        matrices.popPose();
    }

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
