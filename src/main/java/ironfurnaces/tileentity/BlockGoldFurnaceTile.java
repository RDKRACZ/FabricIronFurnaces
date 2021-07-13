package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockGoldFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BlockGoldFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockGoldFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.GOLD_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.goldFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.gold_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockGoldFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }

}
