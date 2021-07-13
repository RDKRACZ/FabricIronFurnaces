package ironfurnaces.blocks;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockObsidianFurnaceTile;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockObsidianFurnace extends BlockIronFurnaceBase {

    public static final String OBSIDIAN_FURNACE = "obsidian_furnace";

    public BlockObsidianFurnace() {
        super(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).hardness(50.0F).resistance(6000.0F));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(world, type, Reference.OBSIDIAN_FURNACE_TILE);
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockObsidianFurnaceTile(pos, state);
    }
}
