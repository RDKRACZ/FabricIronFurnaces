package ironfurnaces.blocks;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockCopperFurnaceTile;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockCopperFurnace extends BlockIronFurnaceBase implements BlockEntityProvider {

    public static final String COPPER_FURNACE = "copper_furnace";

    public BlockCopperFurnace() {
        super(FabricBlockSettings.copyOf(Blocks.GOLD_BLOCK));
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(world, type, Reference.COPPER_FURNACE_TILE);
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockCopperFurnaceTile(pos, state);
    }
}
