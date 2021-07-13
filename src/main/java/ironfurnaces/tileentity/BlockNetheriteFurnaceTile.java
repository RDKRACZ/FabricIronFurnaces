package ironfurnaces.tileentity;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.container.BlockNetheriteFurnaceScreenHandler;
import ironfurnaces.init.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class BlockNetheriteFurnaceTile extends BlockIronFurnaceTileBase {
    public BlockNetheriteFurnaceTile(BlockPos pos, BlockState state) {
        super(Reference.NETHERITE_FURNACE_TILE, pos, state);
    }

    @Override
    protected int getCookTimeConfig() {
        return IronFurnacesConfig.netheriteFurnaceSpeed;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.netherite_furnace";
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockNetheriteFurnaceScreenHandler(i, playerInventory, this, this.propertyDelegate);
    }

}
