package ironfurnaces.tileentity;

import ironfurnaces.container.BlockWirelessHeaterScreenHandler;
import ironfurnaces.init.Reference;
import ironfurnaces.items.ItemHeater;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;
import team.reborn.energy.EnergyTier;

public class BlockWirelessHeaterTile extends TileEntityInventory implements ExtendedScreenHandlerFactory, EnergyStorage, BlockEntityClientSerializable {

    private double energy;
    private int capacity = 100000;



    public BlockWirelessHeaterTile(BlockPos pos, BlockState state) {
        super(Reference.WIRELESS_HEATER_TILE, pos, state, 1);
    }


    public double getEnergy()
    {
        return this.energy;
    }

    public int getCapacity()
    {
        return this.capacity;
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

    public static void tick(World world, BlockPos pos, BlockState state, BlockWirelessHeaterTile e) {
        if (!world.isClient)
        {
            ItemStack stack = e.getStack(0);
            if (!stack.isEmpty()) {
                NbtCompound nbt = new NbtCompound();
                stack.setNbt(nbt);
                nbt.putInt("X", pos.getX());
                nbt.putInt("Y", pos.getY());
                nbt.putInt("Z", pos.getZ());
            }
        }
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        this.energy = compound.getDouble("energy");
        this.capacity = compound.getInt("capacity");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putDouble("energy", this.energy);
        compound.putInt("capacity", this.capacity);
        return compound;
    }

    @Override
    public int[] IgetSlotsForFace(Direction side) {
        return null;
    }

    @Override
    public boolean IcanExtractItem(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public String IgetName() {
        return "container.ironfurnaces.wireless_energy_heater";
    }

    @Override
    public boolean IisItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() instanceof ItemHeater;
    }

    @Override
    public ScreenHandler IcreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockWirelessHeaterScreenHandler(i, playerEntity.getInventory(), this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public double getStored(EnergySide face) {
        return energy;
    }

    @Override
    public void setStored(double amount) {
        energy = amount;
        if (energy > getMaxStoredPower())
        {
            energy = getMaxStoredPower();
        }
        if (energy < 0)
        {
            energy = 0;
        }
        this.markDirty();
    }

    @Override
    public double getMaxStoredPower() {
        return capacity;
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.INSANE;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (hasWorld() && getWorld() instanceof ServerWorld) {
            sync();
        }
    }


}
