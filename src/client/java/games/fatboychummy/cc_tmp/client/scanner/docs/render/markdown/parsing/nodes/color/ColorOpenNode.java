package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color;


import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class ColorOpenNode extends Node {
    private final int color;

    public ColorOpenNode(BasedSequence chars, int color) {
        super(chars);
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return Node.EMPTY_SEGMENTS;
    }
}
