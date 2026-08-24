package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MarkdownSpan(String text, MDStyle style) {
    private static final Map<MarkdownSpan, Component> CACHE = new HashMap<>();

    public @NotNull Component createComponent() {
        return CACHE.computeIfAbsent(this, span ->
                Component.literal(text).withStyle(
                        Style.EMPTY
                                .withBold(style.bold)
                                .withItalic(style.italic)
                                .withStrikethrough(style.strikethrough)
                                .withColor(style.color)
                                .withUnderlined(style.underline)
        ));
    }

    /**
     * Like createComponent, but creates a component for each character.\
     * Does not cache!
     * @return All the components.
     */
    public @NotNull Component[] createComponents() {
        List<Component> components = new ArrayList<>();

        for (char c : text.toCharArray()) {
            components.add(
                    Component.literal(String.valueOf(c)).withStyle(
                            Style.EMPTY
                                    .withBold(style.bold)
                                    .withItalic(style.italic)
                                    .withStrikethrough(style.strikethrough)
                                    .withColor(style.color)
                                    .withUnderlined(style.underline)
                    )
            );
        }

        return components.toArray(Component[]::new);
    }

    public static void destroyCache() {
        CACHE.clear();
    }
}
