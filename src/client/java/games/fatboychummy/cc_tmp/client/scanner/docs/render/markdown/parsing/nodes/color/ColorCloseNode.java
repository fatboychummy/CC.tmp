package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class ColorCloseNode extends Node {
    @Override
    public @NotNull BasedSequence[] getSegments() {
        return BasedSequence.EMPTY_SEGMENTS;
    }
}
