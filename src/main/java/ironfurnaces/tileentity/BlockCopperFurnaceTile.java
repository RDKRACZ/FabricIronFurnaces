package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockCopperFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCopperFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockCopperFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.COPPER_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.copperFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.copper_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockCopperFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }


}
