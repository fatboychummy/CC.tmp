package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style;

import org.jetbrains.annotations.Nullable;

public class MDStyle {
    public static final MDStyle EMPTY = new MDStyle(
            0xFFFFFF,
            1.0F,
            false,
            false,
            false,
            false,
            false,
            null,
            null
    );

    public MDStyle(
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
        this.color = color;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.monospace = monospace;
        this.hover = hover;
        this.click = click;
    }

    public MDStyle withColor(int color) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withSize(float size) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withBold(boolean bold) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withItalic(boolean italic) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withUnderline(boolean underline) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withStrikethrough(boolean strikethrough) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withMonospace(boolean monospace) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withHover(@Nullable HoverData hover) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public MDStyle withClick(@Nullable ClickData click) {
        return new MDStyle(
                color,
                size,
                bold,
                italic,
                underline,
                strikethrough,
                monospace,
                hover,
                click
        );
    }

    public final static float SIZE_MULTIPLIER = 0.5F;
    public final int color;
    public final float size;
    public final boolean bold;
    public final boolean italic;
    public final boolean underline;
    public final boolean strikethrough;
    public final boolean monospace;

    public final @Nullable HoverData hover;
    public final @Nullable ClickData click;
}
