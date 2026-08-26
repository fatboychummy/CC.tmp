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
    }

    private static void renderNetworkNodes(
            PoseStack matrices,
            Camera camera,
            SimpleWiredNetwork network
    ) {
        Vec3 cameraPos = camera.getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_OVERLAY);

        int color = network.getColor();

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

        VertexConsumer consumer = buffer.getBuffer(GoggleRenderTypes.GOGGLE_OVERLAY);
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

        buffer.endBatch(GoggleRenderTypes.GOGGLE_OVERLAY);
    }

    private static void renderNetworkNametags(PoseStack matrices, Camera camera, SimpleWiredNetwork network) {

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

    private static void renderCircle(
            VertexConsumer consumer,
            Matrix4f pose,
            int color,
            int segments,
            float radius
    ) {
        for (int i = 0; i < segments; i++) {
            double a1 = 2.0 * Math.PI * i / segments;
            double a2 = 2.0 * Math.PI * (i + 1) / segments;

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

    // Generates an overlay of a leaf node (circle-shaped with smaller circle) at the current position.
    private static void overlayLeaf(VertexConsumer consumer, Camera camera, PoseStack matrices, int color) {
        // Nothing right now. Will eventually render a circle or something.
        //overlayNode(consumer, pose, color);

        matrices.pushPose();
        matrices.mulPose(camera.rotation());
        Matrix4f pose = matrices.last().pose();

        final int segments = 32;
        float radius = 0.5f;

        renderCircle(consumer, pose, color, segments, radius);

        // Render a smaller circle on top.
        final int white = 0xffffffff;
        radius /= 2.0f;
        renderCircle(consumer, pose, white, segments, radius);

        matrices.popPose();
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
