package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class ColorExtension implements Parser.ParserExtension {
    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customInlineParserExtensionFactory(
                new ColorInlineParserExtension.Factory()
        );
    }
}
