package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.renderer;

import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.MarkdownSpan;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class MarkdownRenderer {
    private RenderContext renderContext;
    private boolean ready = false;
    private final List<MarkdownSpan> spans;

    public MarkdownRenderer(List<MarkdownSpan> spans) {
        this.spans = spans;
    }

    public void setup(Font font, GuiGraphics gfx) {
        renderContext = new RenderContext(font, gfx);
        ready = true;
    }
    public boolean setupNeeded() {
        return !ready;
    }

    public void render(
            int mouseX, int mouseY,
            float partialTick
    ) {
        renderContext.reset();
        renderContext.mousePosition(mouseX, mouseY);
        renderContext.partialTick(partialTick);
        for (MarkdownSpan span : spans) {
            renderContext.draw(span);
        }
    }
}
