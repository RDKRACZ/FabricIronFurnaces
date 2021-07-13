package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockSilverFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BlockSilverFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockSilverFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.SILVER_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.silverFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.silver_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockSilverFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }

}
