package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockIronFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BlockIronFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockIronFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.IRON_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.ironFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.iron_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockIronFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }

}
