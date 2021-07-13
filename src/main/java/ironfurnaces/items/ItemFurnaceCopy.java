package ironfurnaces.items;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemFurnaceCopy extends Item {


    public ItemFurnaceCopy() {
        super(new Item.Settings().group(Reference.itemGroup));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt()) {
            tooltip.add(new LiteralText("Down: " + stack.getNbt().getIntArray("settings")[0]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("Up: " + stack.getNbt().getIntArray("settings")[1]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("North: " + stack.getNbt().getIntArray("settings")[2]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("South: " + stack.getNbt().getIntArray("settings")[3]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("West: " + stack.getNbt().getIntArray("settings")[4]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("East: " + stack.getNbt().getIntArray("settings")[5]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("Auto Input: " + stack.getNbt().getIntArray("settings")[6]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("Auto Output: " + stack.getNbt().getIntArray("settings")[7]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("Redstone Mode: " + stack.getNbt().getIntArray("settings")[8]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
            tooltip.add(new LiteralText("Redstone Value: " + stack.getNbt().getIntArray("settings")[9]).setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
        }
        tooltip.add(new LiteralText("Right-click to copy settings"));
        tooltip.add(new LiteralText("Sneak & right-click to apply settings"));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        if (!ctx.getPlayer().isSneaking())
        {
            return super.useOnBlock(ctx);
        }
        if (!world.isClient) {
            BlockEntity te = world.getBlockEntity(pos);

            if (!(te instanceof BlockIronFurnaceTileBase)) {
                return super.useOnBlock(ctx);
            }

            ItemStack stack = ctx.getStack();
            if (stack.hasNbt())
            {
                int[] settings = stack.getNbt().getIntArray("settings");
                for (int i = 0; i < settings.length; i++)
                    ((BlockIronFurnaceTileBase) te).furnaceSettings.set(i, settings[i]);
            }
            world.updateListeners(pos, world.getBlockState(pos).getBlock().getDefaultState(), world.getBlockState(pos), 3);
            ctx.getPlayer().sendMessage(new LiteralText("Settings applied"), true);
        }

        return super.useOnBlock(ctx);
    }
}
