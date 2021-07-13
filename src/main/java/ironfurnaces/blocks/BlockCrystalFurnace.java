package ironfurnaces.blocks;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockCrystalFurnaceTile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockCrystalFurnace extends BlockIronFurnaceBase {

    public static final String CRYSTAL_FURNACE = "crystal_furnace";
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;


    public BlockCrystalFurnace() {
        super(FabricBlockSettings.copyOf(Blocks.PRISMARINE).nonOpaque());
        this.setDefaultState(this.getDefaultState().with(Properties.LIT, false).with(WATERLOGGED, Boolean.valueOf(false)));

    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return (BlockState) this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite()).with(WATERLOGGED, Boolean.valueOf(fluidState.getFluid() == Fluids.WATER));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getY();
        double d2 = (double) pos.getZ() + 0.5D;

        Direction direction = state.get(Properties.HORIZONTAL_FACING);
        Direction.Axis direction$axis = direction.getAxis();
        double d3 = 0.52D;
        double d4 = random.nextDouble() * 0.6D - 0.3D;
        double d5 = direction$axis == Direction.Axis.X ? (double) direction.getOffsetX() * 0.52D : d4;
        double d6 = random.nextDouble() * 6.0D / 16.0D;
        double d7 = direction$axis == Direction.Axis.Z ? (double) direction.getOffsetZ() * 0.52D : d4;
        world.addParticle(ParticleTypes.PORTAL, d0 + d5, d1 + d6 - 0.5D, d2 + d7, 0.0D, 0.0D, 0.0D);
        world.addParticle(ParticleTypes.PORTAL, d0 + d5, d1 + d6 - 0.5D, d2 + d7, 0.0D, 0.0D, 0.0D);

        super.randomDisplayTick(state, world, pos, random);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(world, type, Reference.CRYSTAL_FURNACE_TILE);
    }


    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockCrystalFurnaceTile(pos, state);
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.WATERLOGGED);
        super.appendProperties(builder);
    }

}
