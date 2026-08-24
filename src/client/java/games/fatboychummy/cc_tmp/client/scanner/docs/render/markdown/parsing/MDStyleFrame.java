package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing;

import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.ClickData;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.HoverData;
import org.jetbrains.annotations.Nullable;

public class MDStyleFrame {
    @Nullable private Integer color;
    @Nullable private Float size;
    @Nullable private Boolean bold;
    @Nullable private Boolean italic;
    @Nullable private Boolean underline;
    @Nullable private Boolean strikethrough;
    @Nullable private Boolean monospace;
    @Nullable private HoverData hover;
    @Nullable private ClickData click;

    public @Nullable Integer color() { return color; }
    public @Nullable Float size() { return size; }
    public @Nullable Boolean bold() { return bold; }
    public @Nullable Boolean italic() { return italic; }
    public @Nullable Boolean underline() { return underline; }
    public @Nullable Boolean strikethrough() { return strikethrough; }
    public @Nullable Boolean monospace() { return monospace; }
    public @Nullable HoverData hover() { return hover; }
    public @Nullable ClickData click() { return click; }

    public MDStyleFrame(
            @Nullable Integer color,
            @Nullable Float size,
            @Nullable Boolean bold,
            @Nullable Boolean italic,
            @Nullable Boolean underline,
            @Nullable Boolean strikethrough,
            @Nullable Boolean monospace,
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

    public static MDStyleFrame defaultFrame() {
        return new MDStyleFrame(
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

    public static MDStyleFrame emptyFrame() {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static MDStyleFrame colorFrame(int color) {
        return new MDStyleFrame(
                color,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public MDStyleFrame colorThis(int color) {
        this.color = color;
        return this;
    }

    public static MDStyleFrame sizeFrame(float size) {
        return new MDStyleFrame(
                null,
                size,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public MDStyleFrame sizeThis(float size) {
        this.size = size;
        return this;
    }

    public static MDStyleFrame boldFrame(boolean bold) {
        return new MDStyleFrame(
                null,
                null,
                bold,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public MDStyleFrame boldThis(boolean bold) {
        this.bold = bold;
        return this;
    }

    public static MDStyleFrame italicFrame(boolean italic) {
        return new MDStyleFrame(
                null,
                null,
                null,
                italic,
                null,
                null,
                null,
                null,
                null
        );
    }

    public MDStyleFrame italicThis(boolean italic) {
        this.italic = italic;
        return this;
    }

    public static MDStyleFrame underlineFrame(boolean underline) {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                underline,
                null,
                null,
                null,
                null
        );
    }

    public MDStyleFrame underlineThis(boolean underline) {
        this.underline = underline;
        return this;
    }

    public static MDStyleFrame strikethroughFrame(boolean strikethrough) {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                null,
                strikethrough,
                null,
                null,
                null
        );
    }

    public MDStyleFrame strikethroughThis(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public static MDStyleFrame monospaceFrame(boolean monospace) {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                null,
                null,
                monospace,
                null,
                null
        );
    }

    public MDStyleFrame monospaceThis(boolean monospace) {
        this.monospace = monospace;
        return this;
    }

    public static MDStyleFrame hoverFrame(HoverData hoverData) {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                hoverData,
                null
        );
    }

    public MDStyleFrame hoverThis(HoverData hoverData) {
        this.hover = hoverData;
        return this;
    }

    public static MDStyleFrame clickFrame(ClickData clickData) {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                clickData
        );
    }

    public MDStyleFrame clickThis(ClickData clickData) {
        this.click = clickData;
        return this;
    }
}
