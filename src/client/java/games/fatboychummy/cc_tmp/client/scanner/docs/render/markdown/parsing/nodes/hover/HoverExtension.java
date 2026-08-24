package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class HoverExtension implements Parser.ParserExtension {
    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customInlineParserExtensionFactory(
                new HoverInlineParserExtension.Factory()
        );
    }
}
