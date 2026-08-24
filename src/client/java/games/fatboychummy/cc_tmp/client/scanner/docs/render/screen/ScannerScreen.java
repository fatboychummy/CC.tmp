package games.fatboychummy.cc_tmp.client.scanner.docs.render.screen;

import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.MarkdownParser;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.MarkdownVisitor;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.renderer.MarkdownRenderer;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.MarkdownSpan;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScannerScreen extends Screen {
    private final MarkdownRenderer markdownRenderer;

    @Override
    public void onClose() {
        super.onClose();
        MarkdownSpan.destroyCache(); // no memory leak for u
    }

    public ScannerScreen(Node markdown) {
        super(Component.translatable("gui.scan_finished.title"));

        MarkdownVisitor.reset();
        NodeVisitor visitor = MarkdownVisitor.getVisitor();
        visitor.visit(markdown);
        MarkdownVisitor.dump(markdown);

        markdownRenderer = new MarkdownRenderer(MarkdownVisitor.getSpans());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        if (markdownRenderer.setupNeeded()) markdownRenderer.setup(font, guiGraphics);
        markdownRenderer.render(mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
