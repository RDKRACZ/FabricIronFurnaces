package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockCrystalFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BlockCrystalFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockCrystalFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.CRYSTAL_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.crystalFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.crystal_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockCrystalFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }

}
