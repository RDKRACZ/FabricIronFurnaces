package ironfurnaces.blocks;

import ironfurnaces.items.ItemAugment;
import ironfurnaces.items.ItemFurnaceCopy;
import ironfurnaces.items.ItemSpooky;
import ironfurnaces.items.ItemXmas;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.ToIntFunction;

public abstract class BlockIronFurnaceBase extends Block implements BlockEntityProvider {

    public static final IntProperty TYPE = IntProperty.of("type", 0, 2);
    public static final IntProperty JOVIAL = IntProperty.of("jovial", 0, 2);


    public BlockIronFurnaceBase(FabricBlockSettings properties) {
        super(properties.luminance(createLightLevelFromBlockState(13)));
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(Properties.HORIZONTAL_FACING, Direction.NORTH)).with(Properties.LIT, false).with(TYPE, 0).with(JOVIAL, 0));
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> checkType(World world, BlockEntityType<T> givenType, BlockEntityType<? extends BlockIronFurnaceTileBase> expectedType) {
        return world.isClient ? null : checkType(givenType, expectedType, BlockIronFurnaceTileBase::tick);
    }

    /**
     * {@return the ticker if the given type and expected type are the same, or {@code null} if they are different}
     */
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }


    private static ToIntFunction<BlockState> createLightLevelFromBlockState(int litLevel) {
        return (blockState) -> {
            return (Boolean)blockState.get(Properties.LIT) ? litLevel : 0;
        };
    }

    private int calculateOutput(World worldIn, BlockPos pos, BlockState state) {
        BlockIronFurnaceTileBase tile = ((BlockIronFurnaceTileBase)worldIn.getBlockEntity(pos));
        int i = this.getComparatorOutput(state, worldIn, pos);
        if (tile != null)
        {
            int j = tile.furnaceSettings.get(9);
            return tile.furnaceSettings.get(8) == 4 ? Math.max(i - j, 0) : i;
        }
        return 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getStrongRedstonePower(BlockState blockState, BlockView world, BlockPos pos, Direction side) {
        return getWeakRedstonePower(blockState, world, pos, side);
    }


    @Override
    public int getWeakRedstonePower(BlockState blockState, BlockView world, BlockPos pos, Direction side) {
        BlockIronFurnaceTileBase furnace = ((BlockIronFurnaceTileBase) world.getBlockEntity(pos));
        if (furnace != null)
        {
            int mode = furnace.furnaceSettings.get(8);
            if (mode == 0)
            {
                return 0;
            }
            else if (mode == 1)
            {
                return 0;
            }
            else if (mode == 2)
            {
                return 0;
            }
            else
            {
                return calculateOutput(furnace.getWorld(), pos, blockState);
            }
        }
        return 0;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BlockIronFurnaceTileBase) {
                ((BlockIronFurnaceTileBase)blockEntity).setCustomName(itemStack.getName());

            }
        }

    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getActiveItem().copy();
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            if (player.getStackInHand(hand).getItem() instanceof ItemAugment && !(player.isSneaking())) {
                this.interactAugment(world, pos, player, hand);
            } else if (player.getStackInHand(hand).getItem() instanceof ItemFurnaceCopy && !(player.isSneaking())) {
                this.interactCopy(world, pos, player, hand);
            } else if (player.getStackInHand(hand).getItem() instanceof ItemSpooky && !(player.isSneaking())) {
                return this.interactJovial(world, pos, player, hand, 1);
            } else if (player.getStackInHand(hand).getItem() instanceof ItemXmas && !(player.isSneaking())) {
                return this.interactJovial(world, pos, player, hand, 2);
            } else if (player.getStackInHand(hand).isEmpty() && player.isSneaking()) {
                return this.interactJovial(world, pos, player, hand, 0);
            } else {
                this.openScreen(world, pos, player);
            }
            return ActionResult.CONSUME;
        }
    }

    private ActionResult interactJovial(World world, BlockPos pos, PlayerEntity player, Hand handIn, int jovial) {
        if (!(player.getStackInHand(handIn).getItem() instanceof ItemSpooky
                || !(player.getStackInHand(handIn).getItem() instanceof ItemXmas)
                || !(player.getStackInHand(handIn).isEmpty()))) {
            return ActionResult.SUCCESS;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof BlockIronFurnaceTileBase)) {
            return ActionResult.SUCCESS;
        }
        ((BlockIronFurnaceTileBase)te).jovial = jovial;
        return ActionResult.SUCCESS;
    }

    private ActionResult interactCopy(World world, BlockPos pos, PlayerEntity player, Hand handIn) {
        ItemStack stack = player.getInventory().getStack(player.getInventory().selectedSlot);
        if (!(stack.getItem() instanceof ItemFurnaceCopy)) {
            return ActionResult.SUCCESS;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof BlockIronFurnaceTileBase)) {
            return ActionResult.SUCCESS;
        }

        int[] settings = new int[((BlockIronFurnaceTileBase) te).furnaceSettings.size()];
        for (int i = 0; i < ((BlockIronFurnaceTileBase) te).furnaceSettings.size(); i++)
        {
            settings[i] = ((BlockIronFurnaceTileBase) te).furnaceSettings.get(i);
        }
        stack.getOrCreateNbt().putIntArray("settings", settings);

        ((BlockIronFurnaceTileBase)te).onUpdateSent();
        player.sendMessage(new LiteralText("Settings copied"), true);
        return ActionResult.SUCCESS;
    }

    private ActionResult interactAugment(World world, BlockPos pos, PlayerEntity player, Hand handIn) {
        if (!(player.getStackInHand(handIn).getItem() instanceof ItemAugment)) {
            return ActionResult.SUCCESS;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof BlockIronFurnaceTileBase)) {
            return ActionResult.SUCCESS;
        }
        if (!(((Inventory) te).getStack(3).isEmpty())) {
            if (!player.isCreative()) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), ((Inventory) te).getStack(3)));
            }
        }
        ((Inventory) te).setStack(3, new ItemStack(player.getStackInHand(handIn).getItem(), 1));

        world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.8F, 1.0F);

        if (!player.isCreative()) {
            player.getStackInHand(handIn).decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BlockIronFurnaceTileBase) {
            ((BlockIronFurnaceTileBase)blockEntity).placeConfig();
            player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_FURNACE);
        }

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if ((Boolean)state.get(Properties.LIT)) {
            double d = (double)pos.getX() + 0.5D;
            double e = (double)pos.getY();
            double f = (double)pos.getZ() + 0.5D;
            if (random.nextDouble() < 0.1D) {
                world.playSound(d, e, f, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = (Direction)state.get(Properties.HORIZONTAL_FACING);
            Direction.Axis axis = direction.getAxis();
            double g = 0.52D;
            double h = random.nextDouble() * 0.6D - 0.3D;
            double i = axis == Direction.Axis.X ? (double)direction.getOffsetX() * 0.52D : h;
            double j = random.nextDouble() * 6.0D / 16.0D;
            double k = axis == Direction.Axis.Z ? (double)direction.getOffsetZ() * 0.52D : h;
            world.addParticle(ParticleTypes.SMOKE, d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
            world.addParticle(ParticleTypes.FLAME, d + i, e + j, f + k, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BlockIronFurnaceTileBase) {
                ItemScatterer.spawn(world, (BlockPos)pos, (Inventory)((BlockIronFurnaceTileBase)blockEntity));
                ((BlockIronFurnaceTileBase)blockEntity).method_27354(world, Vec3d.ofCenter(pos));
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(Properties.HORIZONTAL_FACING, rotation.rotate((Direction)state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING, Properties.LIT, TYPE, JOVIAL);
    }
}
