package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover;


import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class HoverOpenNode extends Node {
    private final String hoverText;

    public HoverOpenNode(BasedSequence chars, String hoverText) {
        super(chars);
        this.hoverText = hoverText;
    }

    public String getHoverText() {
        return hoverText;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return Node.EMPTY_SEGMENTS;
    }
}
