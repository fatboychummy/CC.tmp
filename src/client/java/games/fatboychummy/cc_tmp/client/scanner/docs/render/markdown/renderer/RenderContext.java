package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.MDStyle;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.MarkdownSpan;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RenderContext {
    private static final int MIN_X = 10;
    private static final int MIN_Y = 10;
    private static final float GLOBAL_SCALE = 0.5f;

    private Position pos;
    private float scale = 1;
    private int mouseX;
    private int mouseY;
    private float partialTick;

    private final int width;
    private final int height;
    private final int baseLineHeight;
    private final Font font;
    private final GuiGraphics gfx;
    private final PoseStack pose;

    public RenderContext(Font font, GuiGraphics gfx) {
        this.font = font;
        this.gfx = gfx;
        baseLineHeight = font.lineHeight * 2 + 2;
        pose = gfx.pose();
        width = gfx.guiWidth();
        height = gfx.guiHeight();
        pos = new Position(MIN_X, MIN_Y);
    }

    private int getModifiedWidth() {
        return (int) (width / scale);
    }
    private int getModifiedHeight() {
        return (int) (height / scale);
    }

    public void mousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void partialTick(float partialTick) {
        this.partialTick = partialTick;
    }

    private class Position {
        private final float x;
        private final float y;

        public int x() {
            return (int) x;
        }
        public int y() {
            return (int) y;
        }
        public float fY() {
            return y;
        }
        public float fX() {
            return x;
        }

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Position add(Position other) {
            return new Position(x + other.x, y + other.y);
        }

        public Position sub(Position other) {
            return new Position(x - other.x, y - other.y);
        }

        public Position mul(Position other) {
            return new Position(x * other.x, y * other.y);
        }

        public Position mul(float other) {
            return new Position(x * other, y * other);
        }

        public Position div(Position other) {
            return new Position(x / other.x, y / other.y);
        }

        public Position div(float other) {
            return new Position(x / other, y / other);
        }

        public Position toGlobal() {
            return this.mul(scale);
        }

        public Position toLocal() {
            return this.div(scale);
        }
    }

    public void reset() {
        pos = new Position(MIN_X, MIN_Y);
        scale = 1;
    }

    private float scale() {
        return scale * GLOBAL_SCALE;
    }

    public void draw(MarkdownSpan span) {
        MDStyle style = span.style();

        scale = style.size;
        float localScale = scale();
        Position localPosition = pos.toLocal();

        if (style.monospace) {
            drawMono(span, localPosition, localScale);

            pose.popPose();
            return;
        }

        pose.pushPose();
        pose.scale(localScale, localScale, 1.0F);
        pose.translate(localPosition.x(), localPosition.y(), 0);

        int localWidth = getModifiedWidth();

        if (span.text().equals("\n") || localPosition.x() > localWidth) {
            pos = new Position(MIN_X, pos.fY() + (baseLineHeight * localScale));

            pose.popPose();
            return;
        }

        Component component = span.createComponent();
        gfx.drawString(
                font,
                component,
                0,
                0,
                0xffffff,
                false
        );

        pose.popPose();
        updatePos(component);
    }

    private void drawMono(MarkdownSpan span, Position localPosition, float localScale) {
        Component[] components = span.createComponents();
        int jumpDistance = font.width("M"); // Probably the widest character? /shrug

        pose.pushPose();
        pose.scale(localScale, localScale, 1.0F);
        pose.translate(localPosition.x(), localPosition.y(), 0);

        for (int i = 0; i < components.length; i++) {
            Component component = components[i];

            pose.pushPose();
            pose.translate(i * (jumpDistance + 2), 0, 0);

            gfx.drawCenteredString(
                    font,
                    component,
                    0,
                    0,
                    0xffffff
            );

            pose.popPose();
            updatePos(component);
            pos = pos.add(
                    new Position(2, 0)
                            .toGlobal()
            );
        }
    }

    private void updatePos(Component component) {
        pos = pos.add(
                new Position(font.width(component), 0)
                        .toGlobal()
        );
    }
}
