package ironfurnaces.items;

import ironfurnaces.init.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class BlockItemFurnace extends BlockItem {
    public BlockItemFurnace(Block block) {
        super(block, new Item.Settings().group(Reference.itemGroup));
    }
}
