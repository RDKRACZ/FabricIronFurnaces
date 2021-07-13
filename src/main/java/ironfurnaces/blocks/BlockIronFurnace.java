package ironfurnaces.blocks;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockGoldFurnaceTile;
import ironfurnaces.tileentity.BlockIronFurnaceTile;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockIronFurnace extends BlockIronFurnaceBase {

    public static final String IRON_FURNACE = "iron_furnace";

    public BlockIronFurnace() {
        super(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(world, type, Reference.IRON_FURNACE_TILE);
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockIronFurnaceTile(pos, state);
    }
}
