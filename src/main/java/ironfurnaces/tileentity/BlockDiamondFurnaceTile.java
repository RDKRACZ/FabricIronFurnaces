package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockDiamondFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BlockDiamondFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockDiamondFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.DIAMOND_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.diamondFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.diamond_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockDiamondFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }

}
