package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing;

import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.ClickData;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.HoverData;
import org.jetbrains.annotations.Nullable;

public record MDFinalizedStyleFrame(
        int color,
        float size,
        boolean bold,
        boolean italic,
        boolean underline,
        boolean strikethrough,
        boolean monospace,
        @Nullable HoverData hover,
        @Nullable ClickData click
) {
    public static MDFinalizedStyleFrame defaultFrame() {
        return new MDFinalizedStyleFrame(
                0xffffff,
                1.0f,
                false,
                false,
                false,
                false,
                false,
                null,
                null
        );
    }
}
