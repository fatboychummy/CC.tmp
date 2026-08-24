package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class HoverCloseNode extends Node {
    @Override
    public @NotNull BasedSequence[] getSegments() {
        return BasedSequence.EMPTY_SEGMENTS;
    }
}
