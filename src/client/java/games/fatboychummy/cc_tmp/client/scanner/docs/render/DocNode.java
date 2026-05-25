package games.fatboychummy.cc_tmp.client.scanner.docs.render;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public interface DocNode {
/*
sealed interface DocNode permits HeaderNode, ParagraphNode, ListNode, ItemIconNode, HyperlinkNode, CustomTextNode, WidgetNode {
    /**
     * Gets the absolute raw text of this object. For example, if a hyperlink is given by `[text text](url)`, this should return just `text text`.
     * @return The raw text.
     */
    @NotNull String getRawText();

    /**
     * Get the width of this object.
     * @return The width.
     */
    @NotNull Integer getWidth();

    /**
     * Get the height of this object.
     * @return The height.
     */
    @NotNull Integer getHeight();

    /**
     * Render this object at the given location.
     * @param gfx The GuiGraphics object.
     * @param atX The X position at which this object should be rendered.
     * @param atY The Y position at which this object should be rendered.
     * @param mouseX The X position of the mouse.
     * @param mouseY The Y position of the mouse.
     * @param partialTick The partial tick we are on.
     */
    void render(GuiGraphics gfx, int atX, int atY, int mouseX, int mouseY, float partialTick);
}
