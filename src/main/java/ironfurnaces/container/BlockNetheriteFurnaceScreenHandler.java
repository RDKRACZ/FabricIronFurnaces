package ironfurnaces.container;

import ironfurnaces.init.Reference;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;

public class BlockNetheriteFurnaceScreenHandler extends BlockIronFurnaceScreenHandlerBase {


    public BlockNetheriteFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Reference.NETHERITE_FURNACE_SCREEN_HANDLER, syncId, playerInventory, buf);
    }

    public BlockNetheriteFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(Reference.NETHERITE_FURNACE_SCREEN_HANDLER, syncId, playerInventory, inventory, propertyDelegate);
    }
}
