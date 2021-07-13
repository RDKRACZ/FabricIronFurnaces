package ironfurnaces.rei;

import ironfurnaces.config.IronFurnacesConfig;
import ironfurnaces.gui.*;
import ironfurnaces.init.Reference;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class REIPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        if (IronFurnacesConfig.enableCatalysts) {
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.IRON_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.GOLD_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.DIAMOND_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.EMERALD_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.CRYSTAL_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.OBSIDIAN_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.NETHERITE_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.COPPER_FURNACE)));
            addWorkstations(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.SILVER_FURNACE)));

            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.IRON_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.GOLD_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.DIAMOND_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.EMERALD_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.CRYSTAL_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.OBSIDIAN_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.NETHERITE_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.COPPER_FURNACE)));
            addWorkstationsFuel(EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(Reference.SILVER_FURNACE)));
        }
    }
    private void addWorkstations(EntryStack<?>... stacks) {
        CategoryRegistry.getInstance().addWorkstations(CategoryIdentifier.of(new Identifier("minecraft", "plugins/smelting")), stacks);
    }

    private void addWorkstationsFuel(EntryStack<?>... stacks) {
        CategoryRegistry.getInstance().addWorkstations(CategoryIdentifier.of(new Identifier("minecraft", "plugins/fuel")), stacks);
    }

    @Override
    public void registerScreens(me.shedaniel.rei.api.client.registry.screen.ScreenRegistry registry) {
        if (IronFurnacesConfig.enableClickArea)
        {
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockIronFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockGoldFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockDiamondFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockEmeraldFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockCrystalFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockObsidianFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockNetheriteFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockCopperFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
            registry.registerContainerClickArea(new me.shedaniel.math.Rectangle(79, 35, 24, 17), BlockSilverFurnaceScreen.class, CategoryIdentifier.of("minecraft", "plugins/smelting"));
        }
    }
}
