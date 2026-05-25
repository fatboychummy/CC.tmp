package games.fatboychummy.cc_tmp.block;

import games.fatboychummy.cc_tmp.Cc_tmp;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class tmpBlocks {
    public static final Block SCANNER_OVERLAY = registerBlock(
            "scanner_overlay",
            new ScannerOverlayBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK))
    );

    private static Block registerBlock(String name, Block block) {
        return Registry.register(
                BuiltInRegistries.BLOCK,
                Cc_tmp.id(name),
                block
        );
    }

    public static void initBlocks() {
        // nothing
    }
}
