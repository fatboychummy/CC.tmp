package games.fatboychummy.cc_tmp;

import com.mojang.logging.LogUtils;
import games.fatboychummy.cc_tmp.block.tmpBlocks;
import games.fatboychummy.cc_tmp.item.tmpItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class Cc_tmp implements ModInitializer {
    public static String MOD_ID = "cc_tmp";
    public static Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String ext) {
        return new ResourceLocation(MOD_ID, ext);
    }

    @Override
    public void onInitialize() {
        tmpItems.initItems();
        tmpBlocks.initBlocks();
    }
}
