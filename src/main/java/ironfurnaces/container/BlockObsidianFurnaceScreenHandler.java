package ironfurnaces.container;

import ironfurnaces.init.Reference;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;

public class BlockObsidianFurnaceScreenHandler extends BlockIronFurnaceScreenHandlerBase {


    public BlockObsidianFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Reference.OBSIDIAN_FURNACE_SCREEN_HANDLER, syncId, playerInventory, buf);
    }

    public BlockObsidianFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(Reference.OBSIDIAN_FURNACE_SCREEN_HANDLER, syncId, playerInventory, inventory, propertyDelegate);
    }
}
