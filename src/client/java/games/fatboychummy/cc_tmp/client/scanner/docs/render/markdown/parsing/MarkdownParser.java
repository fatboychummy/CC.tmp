package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color.ColorExtension;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color.ColorInlineParserExtension;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover.HoverInlineParserExtension;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/*
    Notes to self about required markdown objects:
    1. On-hover "dropdown" object.
        This object, when hovered, will display information.
        Used for shortDescription of method argument/return values.
        May be used elsewhere.
        Ties into...
    2. On-click "dropdown" object.
        This object, when clicked, will display information.
        Used for long description of method argument/return values.
        May be used elsewhere.
    3. Combo of the above two nodes.
        For locations where we need both (quite a bit of places!), would be nice to have a single node handle both of them.
    4. Color node
        Usage will vary throughout.
 */

/**
 * Simple class to wrap Commonmark's parser.
 * Allows us to "globally" modify the parser.
 */
public abstract class MarkdownParser {
    private static final Parser parser = Parser.builder()
            .customInlineParserExtensionFactory(new ColorInlineParserExtension.Factory())
            .customInlineParserExtensionFactory(new HoverInlineParserExtension.Factory())
            .build();
    public static Node parse(String input) {
        return parser.parse(input);
    }
    public static Node parseReader(Reader reader) throws IOException {
        return parser.parseReader(reader);
    }
}
