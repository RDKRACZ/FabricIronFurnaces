package ironfurnaces.tileentity;

import com.google.common.collect.Lists;
import ironfurnaces.blocks.BlockIronFurnaceBase;
import ironfurnaces.config.FurnaceSettings;
import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.init.Reference;
import ironfurnaces.items.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.*;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.Energy;

import java.util.*;

public abstract class BlockIronFurnaceTileBase extends TileEntityInventory implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, RecipeUnlocker, RecipeInputProvider {
    public static final int INPUT = 0;
    public static final int FUEL = 1;
    public static final int OUTPUT = 2;

    public final int[] provides = new int[Direction.values().length];
    private final int[] lastProvides = new int[this.provides.length];

    private int timer;
    private int currentAugment = 0; // 0 == none 1 == Blasting 2 == Smoking 3 == Speed 4 == Fuel
    /**
     * The number of ticks that the furnace will keep burning
     */
    private int furnaceBurnTime;
    /**
     * The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for
     */
    private int currentItemBurnTime;
    private int cookTime;
    private int totalCookTime = this.getCookTime();
    private final Object2IntOpenHashMap<Identifier> recipesUsed;
    protected final PropertyDelegate propertyDelegate;
    public int jovial;
    public Random rand = new Random();

    protected RecipeType<? extends AbstractCookingRecipe> recipeType;

    public FurnaceSettings furnaceSettings;
    public boolean placedConfig = false;


    public BlockIronFurnaceTileBase(BlockEntityType<?> tileentitytypeIn, BlockPos pos, BlockState state) {
        super(tileentitytypeIn, pos, state, 4);
        furnaceSettings = new FurnaceSettings() {
            @Override
            public void onChanged() {
                markDirty();
            }
        };
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0:
                        return BlockIronFurnaceTileBase.this.furnaceBurnTime;
                    case 1:
                        return BlockIronFurnaceTileBase.this.currentItemBurnTime;
                    case 2:
                        return BlockIronFurnaceTileBase.this.cookTime;
                    case 3:
                        return BlockIronFurnaceTileBase.this.totalCookTime;
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        BlockIronFurnaceTileBase.this.furnaceBurnTime = value;
                        break;
                    case 1:
                        BlockIronFurnaceTileBase.this.currentItemBurnTime = value;
                        break;
                    case 2:
                        BlockIronFurnaceTileBase.this.cookTime = value;
                        break;
                    case 3:
                        BlockIronFurnaceTileBase.this.totalCookTime = value;
                }

            }

            public int size() {
                return 4;
            }
        };
        this.recipesUsed = new Object2IntOpenHashMap();
        this.recipeType = RecipeType.SMELTING;
    }

    public void forceUpdateAllStates() {
        BlockState state = world.getBlockState(pos);
        if (state.get(Properties.LIT) != furnaceBurnTime > 0) {
            world.setBlockState(pos, state.with(Properties.LIT, furnaceBurnTime > 0), 3);
        }
        if (state.get(BlockIronFurnaceBase.TYPE) != getStateType()) {
            world.setBlockState(pos, state.with(BlockIronFurnaceBase.TYPE, getStateType()), 3);
        }
        if (state.get(BlockIronFurnaceBase.JOVIAL) != jovial) {
            world.setBlockState(pos, state.with(BlockIronFurnaceBase.JOVIAL, jovial), 3);
        }
    }

    protected int getCookTime() {
        ItemStack stack = this.getStack(3);
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemAugmentSpeed || stack.getItem() instanceof ItemAugmentBlasting || stack.getItem() instanceof ItemAugmentSmoking) {
                return getCookTimeConfig() / 2;
            }
            if (stack.getItem() instanceof ItemAugmentFuel) {
                return (int) (getCookTimeConfig() * 1.25);
            }
        }
        return getCookTimeConfig();
    }

    protected int getCookTimeConfig() {
        return 200;
    }

    private int getAugment(ItemStack stack) {
        if (stack.getItem() instanceof ItemAugmentBlasting) {
            return 1;
        } else if (stack.getItem() instanceof ItemAugmentSmoking) {
            return 2;
        } else if (stack.getItem() instanceof ItemAugmentSpeed) {
            return 3;
        } else if (stack.getItem() instanceof ItemAugmentFuel) {
            return 4;
        }
        return 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockIronFurnaceTileBase e) {
        if (e.furnaceSettings.size() <= 0) {
            e.furnaceSettings = new FurnaceSettings() {
                @Override
                public void onChanged() {
                    e.markDirty();
                }
            };
        }
        boolean flag1 = false;
        if (e.currentAugment != e.getAugment(e.getStack(3))) {
            e.currentAugment = e.getAugment(e.getStack(3));
            e.furnaceBurnTime = 0;
        }
        if (e.isBurning()) {
            --e.furnaceBurnTime;
        }

        if (!e.world.isClient) {
            e.timer++;
            if (e.cookTime <= 0) {
                e.autoIO();
                flag1 = true;
            }
            if (e.totalCookTime != e.getCookTime()) {
                e.totalCookTime = e.getCookTime();
            }
            int mode = e.getRedstoneSetting();
            if (mode != 0) {
                if (mode == 2) {
                    int i = 0;
                    for (Direction side : Direction.values()) {
                        if (e.world.getEmittedRedstonePower(pos.offset(side), side) > 0) {
                            i++;
                        }
                    }
                    if (i != 0) {
                        e.cookTime = 0;
                        e.furnaceBurnTime = 0;
                        e.forceUpdateAllStates();
                        return;
                    }
                }
                if (mode == 1) {
                    boolean flag = false;
                    for (Direction side : Direction.values()) {

                        if (e.world.getEmittedRedstonePower(pos.offset(side), side) > 0) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        e.cookTime = 0;
                        e.furnaceBurnTime = 0;
                        e.forceUpdateAllStates();
                        return;
                    }
                }
                for (int i = 0; i < Direction.values().length; i++)
                    e.provides[i] = e.getCachedState().getStrongRedstonePower(e.world, pos, Direction.byId(i));

            } else {
                for (int i = 0; i < Direction.values().length; i++)
                    e.provides[i] = 0;
            }
            if (e.doesNeedUpdateSend()) {
                e.onUpdateSent();
            }
            if (!e.getStack(3).isEmpty()) {
                if (e.getStack(3).getItem() instanceof ItemAugmentBlasting) {
                    if (e.recipeType != RecipeType.BLASTING) {
                        e.recipeType = RecipeType.BLASTING;
                    }
                } else if (e.getStack(3).getItem() instanceof ItemAugmentSmoking) {
                    if (e.recipeType != RecipeType.SMOKING) {
                        e.recipeType = RecipeType.SMOKING;
                    }
                }
            } else {
                if (e.recipeType != RecipeType.SMELTING) {
                    e.recipeType = RecipeType.SMELTING;
                }
            }
            ItemStack itemstack = e.inventory.get(1);
            if (e.isBurning() || !itemstack.isEmpty() && !e.inventory.get(0).isEmpty()) {
                Recipe<?> irecipe = (Recipe) e.world.getRecipeManager().getFirstMatch(e.recipeType, e, e.world).orElse(null);
                if (!e.isBurning() && e.canSmelt(irecipe)) {
                    if (itemstack.getItem() instanceof ItemHeater) {
                        if (itemstack.hasNbt()) {
                            int x = itemstack.getNbt().getInt("X");
                            int y = itemstack.getNbt().getInt("Y");
                            int z = itemstack.getNbt().getInt("Z");
                            BlockEntity te = world.getBlockEntity(new BlockPos(x, y, z));
                            if (te != null && te instanceof BlockWirelessHeaterTile) {
                                double energy = Energy.of(te).getEnergy();
                                if (energy >= IronFurnacesConfig.energy_usage) {
                                    Energy.of(te).extract(IronFurnacesConfig.energy_usage);
                                    int fuel = (getFuelTime(new ItemStack(Items.COAL)) / 8);
                                    if (!e.getStack(3).isEmpty() && e.getStack(3).getItem() instanceof ItemAugmentFuel) {
                                        e.furnaceBurnTime = (fuel * 2) * e.getCookTime() / 200;
                                    } else if (!e.getStack(3).isEmpty() && e.getStack(3).getItem() instanceof ItemAugmentSpeed) {
                                        e.furnaceBurnTime = (fuel / 2) * e.getCookTime() / 200;
                                    } else {
                                        e.furnaceBurnTime = fuel * e.getCookTime() / 200;
                                    }
                                    e.currentItemBurnTime = e.furnaceBurnTime;
                                }
                            }
                        }
                    } else {
                        if (!e.getStack(3).isEmpty() && e.getStack(3).getItem() instanceof ItemAugmentFuel) {
                            e.furnaceBurnTime = 2 * (getFuelTime(itemstack)) * e.getCookTime() / 200;
                        } else if (!e.getStack(3).isEmpty() && e.getStack(3).getItem() instanceof ItemAugmentSpeed) {
                            e.furnaceBurnTime = (getFuelTime(itemstack) / 2) * e.getCookTime() / 200;
                        } else {
                            e.furnaceBurnTime = getFuelTime(itemstack) * e.getCookTime() / 200;
                        }
                    }
                    e.currentItemBurnTime = e.furnaceBurnTime;
                    if (e.isBurning()) {
                        flag1 = true;
                        if (!(itemstack.getItem() instanceof ItemHeater)) {
                            if (!itemstack.isEmpty()) {
                                Item item = itemstack.getItem();
                                itemstack.decrement(1);
                                if (itemstack.isEmpty()) {
                                    Item item1 = item.getRecipeRemainder();
                                    e.inventory.set(1, item1 == null ? ItemStack.EMPTY : new ItemStack(item1));
                                }
                            }
                        }
                    }
                }

                if (e.isBurning() && e.canSmelt(irecipe)) {
                    ++e.cookTime;
                    if (e.cookTime >= e.totalCookTime) {
                        e.cookTime = 0;
                        e.totalCookTime = e.getCookTime();
                        e.smeltItem(irecipe);
                        flag1 = true;
                    }
                } else {
                    e.cookTime = 0;
                }
            } else if (!e.isBurning() && e.cookTime > 0) {
                e.cookTime = MathHelper.clamp(e.cookTime - 2, 0, e.totalCookTime);
            }
            if (e.timer % 24 == 0) {
                BlockState state2 = world.getBlockState(pos);
                if (state2.get(Properties.LIT) != e.furnaceBurnTime > 0) {
                    world.setBlockState(pos, state2.with(Properties.LIT, e.furnaceBurnTime > 0), 3);
                }
                if (state2.get(BlockIronFurnaceBase.TYPE) != e.getStateType()) {
                    world.setBlockState(pos, state2.with(BlockIronFurnaceBase.TYPE, e.getStateType()), 3);
                }
                if (state2.get(BlockIronFurnaceBase.JOVIAL) != e.jovial) {
                    world.setBlockState(pos, state2.with(BlockIronFurnaceBase.JOVIAL, e.jovial), 3);
                }
            }

        }

        if (flag1) {
            e.markDirty();
        }
    }

    private int getStateType() {
        if (this.getStack(3).getItem() == Reference.SMOKING_AUGMENT) {
            return 1;
        } else if (this.getStack(3).getItem() == Reference.BLASTING_AUGMENT) {
            return 2;
        } else {
            return 0;
        }
    }

    public void autoIO() {
        for (Direction dir : Direction.values()) {
            BlockEntity tile = world.getBlockEntity(pos.offset(dir));
            if (tile == null) {
                continue;
            }
            if (this.furnaceSettings.get(dir.getId()) == 1 || this.furnaceSettings.get(dir.getId()) == 2 || this.furnaceSettings.get(dir.getId()) == 3 || this.furnaceSettings.get(dir.getId()) == 4) {
                if (tile != null) {


                    if (this.getAutoInput() != 0 || this.getAutoOutput() != 0) {
                        if (this.getAutoInput() == 1) {
                            if (this.furnaceSettings.get(dir.getId()) == 1 || this.furnaceSettings.get(dir.getId()) == 3) {
                                if (this.getStack(INPUT).getCount() >= this.getMaxCountPerStack()) {
                                    continue;
                                }
                                Inventory inv = getInventoryAt(world, pos.offset(dir));
                                if (inv != null) {

                                    for (int i = 0; i < inv.size(); i++) {
                                        if (inv.getStack(i).isEmpty()) {
                                            continue;
                                        }
                                        if (canExtract(inv, inv.getStack(i), i, dir.getOpposite()))
                                        {
                                            ItemStack stack = transfer(inv, i, this, inv.getStack(i), INPUT, dir);
                                            if (isItemFuel(stack) && getStack(INPUT).isEmpty() || canMergeItems(getStack(INPUT), stack)) {
                                                insertItemInternal(INPUT, stack, false);
                                            }
                                        }

                                    }

                                }
                            }
                            if (this.furnaceSettings.get(dir.getId()) == 4) {
                                if (this.getStack(FUEL).getCount() >= this.getMaxCountPerStack()) {
                                    continue;
                                }
                                Inventory inv = getInventoryAt(world, pos.offset(dir));
                                if (inv != null) {
                                    for (int i = 0; i < inv.size(); i++) {
                                        if (inv.getStack(i).isEmpty()) {
                                            continue;
                                        }
                                        if (canExtract(inv, inv.getStack(i), i, dir.getOpposite()))
                                        {
                                            ItemStack stack = transfer(inv, i, this, inv.getStack(i), FUEL, dir);
                                            if (isItemFuel(stack) && getStack(FUEL).isEmpty() || canMergeItems(getStack(FUEL), stack)) {
                                                insertItemInternal(FUEL, stack, false);
                                            }
                                        }

                                    }

                                }
                            }
                        }
                        if (this.getAutoOutput() == 1) {

                            if (this.furnaceSettings.get(dir.getId()) == 4) {
                                if (this.getStack(FUEL).isEmpty()) {
                                    continue;
                                }
                                ItemStack stack = extractItemInternal(FUEL, 1, true);
                                if (stack.getItem() != Items.BUCKET) {
                                    continue;
                                }
                                Inventory inv = getInventoryAt(world, pos.offset(dir));
                                if (inv != null) {
                                    for (int i = 0; i < inv.size(); i++) {
                                        if (inv.isValid(i, stack) && (inv.getStack(i).isEmpty() || (canMergeItems(inv.getStack(i), stack) && inv.getStack(i).getCount() + stack.getCount() <= inv.getMaxCountPerStack()))) {
                                            transfer(null, 0, inv, extractItemInternal(FUEL, stack.getCount(), false), i, dir);
                                        }
                                    }

                                }
                            }

                            if (this.furnaceSettings.get(dir.getId()) == 2 || this.furnaceSettings.get(dir.getId()) == 3) {
                                if (this.getStack(OUTPUT).isEmpty()) {
                                    continue;
                                }
                                Inventory inv = getInventoryAt(world, pos.offset(dir));
                                if (inv != null) {
                                    for (int i = 0; i < inv.size(); i++) {
                                        ItemStack stack = extractItemInternal(OUTPUT, 64 - inv.getStack(i).getCount(), true);
                                        if (inv.isValid(i, stack) && (inv.getStack(i).isEmpty() || (canMergeItems(inv.getStack(i), stack) && inv.getStack(i).getCount() + stack.getCount() <= inv.getMaxCountPerStack()))) {
                                            transfer(null, 0, inv, extractItemInternal(OUTPUT, 64 - inv.getStack(i).getCount(), false), i, dir);
                                        }
                                    }

                                }


                            }

                        }
                    }
                }
            }
        }
    }


    public ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isValid(slot, stack))
            return stack;

        ItemStack existing = this.inventory.get(slot);

        int limit = stack.getMaxCount();

        if (!existing.isEmpty()) {
            if (!canMergeItems(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.inventory.set(slot, reachedLimit ? new ItemStack(stack.getItem(), limit) : stack);
            } else {
                existing.increment(reachedLimit ? limit : stack.getCount());
            }
            this.markDirty();
        }

        return reachedLimit ? new ItemStack(stack.getItem(), stack.getCount() - limit) : ItemStack.EMPTY;
    }

    private ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        ItemStack existing = this.getStack(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxCount());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.setStack(slot, ItemStack.EMPTY);
                this.markDirty();
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.setStack(slot, new ItemStack(existing.getItem(), existing.getCount() - toExtract));
                this.markDirty();
            }

            return new ItemStack(existing.getItem(), toExtract);
        }
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return getInventoryAt(world, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, double x, double y, double z) {
        Inventory inventory = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider) block).getInventory(blockState, world, blockPos);
        } else if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory) blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, blockPos, true);
                }
            }
        }

        if (inventory == null) {
            List<Entity> list = world.getOtherEntities((Entity) null, new Box(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntityPredicates.VALID_INVENTORIES);
            if (!list.isEmpty()) {
                inventory = (Inventory) list.get(world.random.nextInt(list.size()));
            }
        }

        return (Inventory) inventory;
    }

    private static ItemStack transfer(@Nullable Inventory from, int slotFrom, Inventory to, ItemStack stack, int slot, @Nullable Direction direction) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot, direction)) {
            boolean bl = false;
            if (itemStack.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                if (from != null)
                {
                    from.setStack(slotFrom, ItemStack.EMPTY);
                }
                bl = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = stack.getMaxCount() - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
                bl = j > 0;
            }

            if (bl) {
                to.markDirty();
            }
        }

        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        } else {
            return !(inventory instanceof SidedInventory) || ((SidedInventory) inventory).canInsert(slot, stack, side);
        }
    }

    private static boolean canExtract(Inventory inv, ItemStack stack, int slot, Direction facing) {
        return !(inv instanceof SidedInventory) || ((SidedInventory) inv).canExtract(slot, stack, facing);
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (!first.isOf(second.getItem())) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else if (first.getCount() > first.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areNbtEqual(first, second);
        }
    }

    //CLIENT SYNC
    public int getSettingBottom() {
        return this.furnaceSettings.get(0);
    }

    public int getSettingTop() {
        return this.furnaceSettings.get(1);
    }

    public int getSettingFront() {
        int i = this.getCachedState().get(Properties.HORIZONTAL_FACING).getId();
        return this.furnaceSettings.get(i);
    }

    public int getSettingBack() {
        int i = this.getCachedState().get(Properties.HORIZONTAL_FACING).getOpposite().getId();
        return this.furnaceSettings.get(i);
    }

    public int getSettingLeft() {
        Direction facing = this.getCachedState().get(Properties.HORIZONTAL_FACING);
        if (facing == Direction.NORTH) {
            return this.furnaceSettings.get(Direction.EAST.getId());
        } else if (facing == Direction.WEST) {
            return this.furnaceSettings.get(Direction.NORTH.getId());
        } else if (facing == Direction.SOUTH) {
            return this.furnaceSettings.get(Direction.WEST.getId());
        } else {
            return this.furnaceSettings.get(Direction.SOUTH.getId());
        }
    }

    public int getSettingRight() {
        Direction facing = this.getCachedState().get(Properties.HORIZONTAL_FACING);
        if (facing == Direction.NORTH) {
            return this.furnaceSettings.get(Direction.WEST.getId());
        } else if (facing == Direction.WEST) {
            return this.furnaceSettings.get(Direction.SOUTH.getId());
        } else if (facing == Direction.SOUTH) {
            return this.furnaceSettings.get(Direction.EAST.getId());
        } else {
            return this.furnaceSettings.get(Direction.NORTH.getId());
        }
    }

    public int getIndexFront() {
        int i = this.getCachedState().get(Properties.HORIZONTAL_FACING).getId();
        return i;
    }

    public int getIndexBack() {
        int i = this.getCachedState().get(Properties.HORIZONTAL_FACING).getOpposite().getId();
        return i;
    }

    public int getIndexLeft() {
        Direction facing = this.getCachedState().get(Properties.HORIZONTAL_FACING);
        if (facing == Direction.NORTH) {
            return Direction.EAST.getId();
        } else if (facing == Direction.WEST) {
            return Direction.NORTH.getId();
        } else if (facing == Direction.SOUTH) {
            return Direction.WEST.getId();
        } else {
            return Direction.SOUTH.getId();
        }
    }

    public int getIndexRight() {
        Direction facing = this.getCachedState().get(Properties.HORIZONTAL_FACING);
        if (facing == Direction.NORTH) {
            return Direction.WEST.getId();
        } else if (facing == Direction.WEST) {
            return Direction.SOUTH.getId();
        } else if (facing == Direction.SOUTH) {
            return Direction.EAST.getId();
        } else {
            return Direction.NORTH.getId();
        }
    }

    public int getAutoInput() {
        return this.furnaceSettings.get(6);
    }

    public int getAutoOutput() {
        return this.furnaceSettings.get(7);
    }

    public int getRedstoneSetting() {
        return this.furnaceSettings.get(8);
    }

    public int getRedstoneComSub() {
        return this.furnaceSettings.get(9);
    }

    public int getShowButtons() {
        return this.furnaceSettings.get(10);
    }

    public boolean isBurning() {
        return this.furnaceBurnTime > 0;
    }

    private boolean canSmelt(Recipe<?> recipe) {
        if (!this.inventory.get(0).isEmpty() && recipe != null) {
            ItemStack itemstack = recipe.getOutput();
            if (itemstack.isEmpty()) {
                return false;
            } else {
                ItemStack itemstack1 = this.inventory.get(2);
                if (itemstack1.isEmpty()) {
                    return true;
                } else if (!itemstack1.isItemEqual(itemstack)) {
                    return false;
                } else if (itemstack1.getCount() + itemstack.getCount() <= this.size() && itemstack1.getCount() < itemstack1.getMaxCount()) { // Forge fix: make furnace respect stack sizes in furnace recipes
                    return true;
                } else {
                    return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxCount(); // Forge fix: make furnace respect stack sizes in furnace recipes
                }
            }
        } else {
            return false;
        }
    }

    private void smeltItem(Recipe<?> recipe) {
        timer = 0;
        if (recipe != null && this.canSmelt(recipe)) {
            ItemStack itemstack = this.inventory.get(0);
            ItemStack itemstack1 = recipe.getOutput();
            ItemStack itemstack2 = this.inventory.get(2);
            if (itemstack2.isEmpty()) {
                this.inventory.set(2, itemstack1.copy());
            } else if (itemstack2.getItem() == itemstack1.getItem()) {
                itemstack2.increment(itemstack1.getCount());
            }

            if (!this.world.isClient) {
                this.setLastRecipe(recipe);
            }

            if (itemstack.getItem() == Blocks.WET_SPONGE.asItem() && !this.inventory.get(1).isEmpty() && this.inventory.get(1).getItem() == Items.BUCKET) {
                this.inventory.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemstack.decrement(1);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        this.furnaceBurnTime = tag.getInt("BurnTime");
        this.cookTime = tag.getInt("CookTime");
        this.totalCookTime = tag.getInt("CookTimeTotal");
        this.timer = 0;
        this.currentAugment = tag.getInt("Augment");
        this.currentItemBurnTime = getFuelTime(this.inventory.get(1));
        this.jovial = tag.getInt("jovial");
        NbtCompound compoundTag = tag.getCompound("RecipesUsed");
        Iterator var4 = compoundTag.getKeys().iterator();

        while (var4.hasNext()) {
            String string = (String) var4.next();
            this.recipesUsed.put(new Identifier(string), compoundTag.getInt(string));
        }

        furnaceSettings.readNbt(tag.getCompound("furnace_settings"));
        placedConfig = tag.getBoolean("placed");
        /**
         CompoundNBT energyTag = tag.getCompound("energy");
         energy.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(energyTag));
         **/

        super.readNbt(tag);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putInt("BurnTime", this.furnaceBurnTime);
        tag.putInt("CookTime", this.cookTime);
        tag.putInt("CookTimeTotal", this.totalCookTime);
        tag.putInt("Augment", this.currentAugment);
        tag.putInt("jovial", this.jovial);
        NbtCompound compoundTag = new NbtCompound();
        this.recipesUsed.forEach((identifier, integer) -> {
            compoundTag.putInt(identifier.toString(), integer);
        });
        tag.put("RecipesUsed", compoundTag);

        NbtCompound compoundTag2 = new NbtCompound();
        furnaceSettings.writeNbt(compoundTag2);
        tag.put("furnace_settings", compoundTag2);
        tag.putBoolean("placed", this.placedConfig);
        /**
         energy.ifPresent(h -> {
         CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
         tag.put("energy", compound);
         });
         **/

        return super.writeNbt(tag);
    }


    protected static int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return (Integer) AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
        }
    }

    public static boolean isItemFuel(ItemStack stack) {
        return getFuelTime(stack) > 0 || stack.getItem() instanceof ItemHeater;
    }


    @Override
    public int[] IgetSlotsForFace(Direction side) {
        if (this.furnaceSettings.get(side.getId()) == 0) {
            return new int[]{};
        } else if (this.furnaceSettings.get(side.getId()) == 1) {
            return new int[]{0, 1};
        } else if (this.furnaceSettings.get(side.getId()) == 2) {
            return new int[]{2};
        } else if (this.furnaceSettings.get(side.getId()) == 3) {
            return new int[]{0, 1, 2};
        } else if (this.furnaceSettings.get(side.getId()) == 4) {
            return new int[]{1};
        }
        return new int[]{};
    }

    @Override
    public boolean IcanExtractItem(int index, ItemStack stack, Direction direction) {
        if (this.furnaceSettings.get(direction.getId()) == 0) {
            return false;
        } else if (this.furnaceSettings.get(direction.getId()) == 1) {
            return false;
        } else if (this.furnaceSettings.get(direction.getId()) == 2) {
            return index == 2;
        } else if (this.furnaceSettings.get(direction.getId()) == 3) {
            return index == 2;
        } else if (this.furnaceSettings.get(direction.getId()) == 4 && stack.getItem() != Items.BUCKET) {
            return false;
        } else if (this.furnaceSettings.get(direction.getId()) == 4 && stack.getItem() == Items.BUCKET) {
            return true;
        }
        return false;
    }

    @Override
    public boolean IisItemValidForSlot(int index, ItemStack stack) {
        if (index == OUTPUT || index == 3) {
            return false;
        } else if (index == INPUT) {
            return this.world.getRecipeManager().getFirstMatch(this.recipeType, new SimpleInventory(stack), this.world).isPresent();
        } else if (index == FUEL) {
            ItemStack itemstack = this.inventory.get(FUEL);
            return getFuelTime(stack) > 0 || (stack.getItem() == Items.BUCKET && itemstack.getItem() != Items.BUCKET) || stack.getItem() instanceof ItemHeater;
        }
        return false;
    }

    public void setLastRecipe(Recipe<?> recipe) {
        if (recipe != null) {
            Identifier identifier = recipe.getId();
            boolean flag2 = false;
            if (!this.world.isClient) {
                if (this.recipesUsed.size() > IronFurnacesConfig.furnaceXPDropValue) {
                    this.method_27354(world, new Vec3d(pos.getX() + this.rand.nextInt(2) - 1, pos.getY(), pos.getZ() + this.rand.nextInt(2) - 1));
                    this.recipesUsed.clear();
                } else {
                    for (Object2IntMap.Entry<Identifier> entry : this.recipesUsed.object2IntEntrySet()) {
                        if (world.getRecipeManager().get(entry.getKey()).isPresent()) {
                            if (entry.getIntValue() > IronFurnacesConfig.furnaceXPDropValue2) {
                                if (!flag2) {
                                    this.method_27354(world, new Vec3d(pos.getX() + this.rand.nextInt(2) - 1, pos.getY(), pos.getZ() + this.rand.nextInt(2) - 1));
                                }
                                flag2 = true;
                            }
                        }
                    }
                    if (flag2) {
                        this.recipesUsed.clear();
                    }
                }
            }

            this.recipesUsed.addTo(identifier, 1);
        }

    }

    public Recipe<?> getLastRecipe() {
        return null;
    }

    public void unlockLastRecipe(PlayerEntity player) {
    }

    public void dropExperience(PlayerEntity player) {
        List<Recipe<?>> list = this.method_27354(player.world, player.getPos());
        player.unlockRecipes((Collection) list);
        this.recipesUsed.clear();
    }

    public List<Recipe<?>> method_27354(World world, Vec3d vec3d) {
        List<Recipe<?>> list = Lists.newArrayList();
        ObjectIterator var4 = this.recipesUsed.object2IntEntrySet().iterator();

        while (var4.hasNext()) {
            Object2IntMap.Entry<Identifier> entry = (Object2IntMap.Entry) var4.next();
            world.getRecipeManager().get((Identifier) entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                dropExperience(world, vec3d, entry.getIntValue(), ((AbstractCookingRecipe) recipe).getExperience());
            });
        }

        return list;
    }

    private static void dropExperience(World world, Vec3d vec3d, int i, float f) {
        int j = MathHelper.floor((float) i * f);
        float g = MathHelper.fractionalPart((float) i * f);
        if (g != 0.0F && Math.random() < (double) g) {
            ++j;
        }

        while (j > 0) {
            int k = ExperienceOrbEntity.roundToOrbSize(j);
            j -= k;
            world.spawnEntity(new ExperienceOrbEntity(world, vec3d.x, vec3d.y, vec3d.z, k));
        }

    }

    public void provideRecipeInputs(RecipeMatcher finder) {
        Iterator var2 = this.inventory.iterator();

        while (var2.hasNext()) {
            ItemStack itemStack = (ItemStack) var2.next();
            finder.addInput(itemStack);
        }

    }

    protected boolean doesNeedUpdateSend() {
        return !Arrays.equals(this.provides, this.lastProvides);
    }

    public void onUpdateSent() {
        System.arraycopy(this.provides, 0, this.lastProvides, 0, this.provides.length);
        this.world.updateNeighbors(this.pos, getCachedState().getBlock());
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.readNbt(tag);
        world.updateListeners(pos, world.getBlockState(pos).getBlock().getDefaultState(), world.getBlockState(pos), 3);
        this.markDirty();
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        this.markDirty();
        return this.writeNbt(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public void placeConfig() {
        if (placedConfig) return;
        if (this.furnaceSettings != null) {
            this.furnaceSettings.set(0, 2);
            this.furnaceSettings.set(1, 1);
            for (Direction dir : Direction.values()) {
                if (dir != Direction.DOWN && dir != Direction.UP) {
                    this.furnaceSettings.set(dir.getId(), 4);
                }
            }
            world.updateListeners(pos, world.getBlockState(pos).getBlock().getDefaultState(), world.getBlockState(pos), 3);
            placedConfig = true;
        }

    }
}
