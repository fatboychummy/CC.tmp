package games.fatboychummy.cc_tmp.mixin.client;


import games.fatboychummy.cc_tmp.client.goggles.render.GoggleRenderer;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void addDebugInfo(CallbackInfoReturnable<List<String>> cir) {
        int lineCount = GoggleRenderer.getLineCount();
        int nodeCount = GoggleRenderer.getNodeCount();
        int leafCount = GoggleRenderer.getLeafCount();
        int networkCount = GoggleRenderer.getNetworkCount();
        cir.getReturnValue().add("CC:TMP rendering:");
        cir.getReturnValue().add(" Networks: " + networkCount);
        cir.getReturnValue().add(" Nodes: " + nodeCount);
        cir.getReturnValue().add(" Peripherals: " + leafCount);
        cir.getReturnValue().add(" Lines: " + lineCount);
    }
}
