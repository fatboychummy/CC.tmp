package games.fatboychummy.cc_tmp.item;

import games.fatboychummy.cc_tmp.Cc_tmp;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class tmpItems {
    private static final String CREATIVE_TAB_ID = "creative_tab";

    public static final Item PERIPHERAL_GOGGLES = registerItem(
            PeripheralGogglesItem.ID,
            new PeripheralGogglesItem(new Item.Properties())
    );
    public static final Item PERIPHERAL_SCANNER  = registerItem(
            PeripheralScannerItem.ID,
            new PeripheralScannerItem(new Item.Properties())
    );

    public static final CreativeModeTab CC_TMP_CREATIVE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Cc_tmp.id(CREATIVE_TAB_ID),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(PERIPHERAL_SCANNER::getDefaultInstance)
                    .displayItems(tmpItems::addCreativeGroupItems)
                    .build()
    );

    private static void addCreativeGroupItems(CreativeModeTab.ItemDisplayParameters iDP, CreativeModeTab.Output output) {
        output.accept(PERIPHERAL_SCANNER);
        output.accept(PERIPHERAL_GOGGLES);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                Cc_tmp.id(name),
                item
        );
    }

    public static void initItems() {
        // Nothing.
    }
}
