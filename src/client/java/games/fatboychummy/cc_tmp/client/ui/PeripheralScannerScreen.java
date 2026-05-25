package games.fatboychummy.cc_tmp.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

// TODO: I was unaware where to start with Minecraft UI, so had AI generate this starting point.
// This needs to be completely redone and checked for quality.

public class PeripheralScannerScreen extends Screen {
    private final List<String> methods;

    public PeripheralScannerScreen(List<String> methods) {
        super(Component.literal("Peripheral Scanner"));
        this.methods = methods;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);

        int y = 40;

        graphics.drawString(
                font,
                "Peripheral Methods",
                40,
                20,
                0x00FFFF,
                false
        );

        for (String method : methods) {
            graphics.drawString(
                    font,
                    method,
                    40,
                    y,
                    0xFFFFFF,
                    false
            );

            y += 12;
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
