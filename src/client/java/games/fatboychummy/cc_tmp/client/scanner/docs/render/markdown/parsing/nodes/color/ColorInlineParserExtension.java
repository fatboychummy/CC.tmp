package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color;

import com.vladsch.flexmark.parser.*;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import games.fatboychummy.cc_tmp.Cc_tmp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class ColorInlineParserExtension implements InlineParserExtension {
    private static final Pattern OPEN = Pattern.compile("^<color=#([0-9a-fA-F]{6})>");
    private static final Pattern CLOSE = Pattern.compile("^</color>");

    @Override
    public void finalizeDocument(@NotNull InlineParser inlineParser) {
    }

    @Override
    public void finalizeBlock(@NotNull InlineParser inlineParser) {
    }

    @Override
    public boolean parse(@NotNull LightInlineParser inlineParser) {
        int initial = inlineParser.getIndex();
        Cc_tmp.LOGGER.debug("Parse Color called (index {})", initial);
        BasedSequence[] open = inlineParser.matchWithGroups(OPEN);
        if (open != null && open.length > 0) {
            String matched = open[1].toString(); // Result confirmed to be the hex code.
            int color = Integer.parseInt(matched, 16);
            ColorOpenNode node = new ColorOpenNode(BasedSequence.EMPTY, color);

            inlineParser.flushTextNode();
            inlineParser.appendNode(node);
            Cc_tmp.LOGGER.debug("Input: {}", inlineParser.getInput());
            Cc_tmp.LOGGER.debug("currentText: {}", inlineParser.getCurrentText());
            Cc_tmp.LOGGER.debug("Parsed color open {} (to index {})", color, inlineParser.getIndex());
            return true;
        }

        BasedSequence close = inlineParser.match(CLOSE);
        if (close != null) {
            inlineParser.flushTextNode();
            inlineParser.appendNode(new ColorCloseNode());
            Cc_tmp.LOGGER.debug("Parsed color close (to index {})", inlineParser.getIndex());
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
            return new ColorInlineParserExtension();
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
