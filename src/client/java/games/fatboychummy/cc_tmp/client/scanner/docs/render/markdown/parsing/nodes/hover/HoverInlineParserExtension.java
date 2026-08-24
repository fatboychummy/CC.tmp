package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import games.fatboychummy.cc_tmp.Cc_tmp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class HoverInlineParserExtension implements InlineParserExtension {
    private static final Pattern OPEN = Pattern.compile("^<hover=\"(.*?)\">");
    private static final Pattern CLOSE = Pattern.compile("^</hover>");

    @Override
    public void finalizeDocument(@NotNull InlineParser inlineParser) {
    }

    @Override
    public void finalizeBlock(@NotNull InlineParser inlineParser) {
    }

    @Override
    public boolean parse(@NotNull LightInlineParser inlineParser) {
        int initial = inlineParser.getIndex();
        Cc_tmp.LOGGER.debug("Parse Hover called (index {})", initial);
        BasedSequence[] open = inlineParser.matchWithGroups(OPEN);
        if (open != null && open.length > 0) {
            String matched = open[1].toString();
            HoverOpenNode node = new HoverOpenNode(BasedSequence.EMPTY, matched);

            inlineParser.flushTextNode();
            inlineParser.appendNode(node);
            Cc_tmp.LOGGER.debug("Input: {}", inlineParser.getInput());
            Cc_tmp.LOGGER.debug("currentText: {}", inlineParser.getCurrentText());
            Cc_tmp.LOGGER.debug("Parsed hover open '{}' (to index {})", matched, inlineParser.getIndex());
            return true;
        }

        BasedSequence close = inlineParser.match(CLOSE);
        if (close != null) {
            inlineParser.flushTextNode();
            inlineParser.appendNode(new HoverCloseNode());
            Cc_tmp.LOGGER.debug("Parsed hover close (to index {})", inlineParser.getIndex());
            return true;
        }

        return false;
    }

    public static class Factory implements InlineParserExtensionFactory {
        @Override
        public @NotNull CharSequence getCharacters() {
            return "<";
        }

        @Override
        public @NotNull InlineParserExtension apply(@NotNull LightInlineParser inlineParser) {
            return new HoverInlineParserExtension();
        }

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return Set.of();
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return Set.of();
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }
}
