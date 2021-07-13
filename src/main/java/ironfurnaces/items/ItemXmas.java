package ironfurnaces.items;

import ironfurnaces.init.Reference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemXmas extends Item {


    public ItemXmas(Settings properties) {
        super(properties);
    }


    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".xmas_right_click").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".xmas1").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".xmas2").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
    }
}
