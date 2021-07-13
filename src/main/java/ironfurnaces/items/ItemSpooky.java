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

public class ItemSpooky extends Item {


    public ItemSpooky(Settings properties) {
        super(properties);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".spooky_right_click").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".spooky1").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".spooky2").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
        tooltip.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".spooky3").setStyle(Style.EMPTY.withFormatting((Formatting.GRAY))));
    }
}
