package ironfurnaces.container;

import ironfurnaces.init.Reference;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;

public class BlockCopperFurnaceScreenHandler extends BlockIronFurnaceScreenHandlerBase {


    public BlockCopperFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Reference.COPPER_FURNACE_SCREEN_HANDLER, syncId, playerInventory, buf);
    }

    public BlockCopperFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(Reference.COPPER_FURNACE_SCREEN_HANDLER, syncId, playerInventory, inventory, propertyDelegate);
    }

}
