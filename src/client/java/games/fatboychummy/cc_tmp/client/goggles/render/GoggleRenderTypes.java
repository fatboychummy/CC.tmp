package games.fatboychummy.cc_tmp.client.goggles.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;


public class GoggleRenderTypes {
    public static final RenderType GOGGLE_OVERLAY = new GoggleRenderType(
            "goggle_overlay",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderSystem.disableDepthTest();
                RenderSystem.disableCull();

                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
            }
    );

    public static final RenderType GOGGLE_LINES = new GoggleRenderType(
            "goggle_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderSystem.disableDepthTest();
                RenderSystem.disableCull();

                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
            }
    );

    public static final RenderType GOGGLE_TRIS = new GoggleRenderType(
            "goggle_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            false,
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                RenderSystem.disableDepthTest();
                RenderSystem.disableCull();

                RenderSystem.setShader(GameRenderer::getPositionColorShader);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
            }
    );

    private static final class GoggleRenderType extends RenderType {
        private GoggleRenderType(
                String name,
                VertexFormat format,
                VertexFormat.Mode mode,
                int bufferSize,
                boolean affectsCrumbling,
                boolean sortOnUpload,
                Runnable setup,
                Runnable teardown
        ) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, teardown);
        }
    }
}
