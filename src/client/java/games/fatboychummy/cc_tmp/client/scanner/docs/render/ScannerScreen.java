package games.fatboychummy.cc_tmp.client.scanner.docs.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ScannerScreen extends Screen {
    public ScannerScreen(Component component) {
        super(component);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawString(font, title.getString(), 20, 20, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
