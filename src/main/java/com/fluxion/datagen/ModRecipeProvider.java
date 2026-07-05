package com.fluxion.datagen;

import com.fluxion.Fluxion;
import com.fluxion.machine.MachineTier;
import com.fluxion.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        // --- Materials ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLUX_INGOT.get(), 2)
                .pattern("RIR")
                .pattern("IGI")
                .pattern("RIR")
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MACHINE_FRAMES.get(MachineTier.T1).get())
                .pattern("IFI")
                .pattern("F F")
                .pattern("IFI")
                .define('I', Items.IRON_INGOT)
                .define('F', ModItems.FLUX_INGOT.get())
                .unlockedBy("has_flux_ingot", has(ModItems.FLUX_INGOT.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MACHINE_FRAMES.get(MachineTier.T2).get())
                .pattern("GFG")
                .pattern("FMF")
                .pattern("GFG")
                .define('G', Items.GOLD_INGOT)
                .define('F', ModItems.FLUX_INGOT.get())
                .define('M', ModItems.MACHINE_FRAMES.get(MachineTier.T1).get())
                .unlockedBy("has_machine_frame_t1", has(ModItems.MACHINE_FRAMES.get(MachineTier.T1).get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MACHINE_FRAMES.get(MachineTier.T3).get())
                .pattern("DFD")
                .pattern("FMF")
                .pattern("DFD")
                .define('D', Items.DIAMOND)
                .define('F', ModItems.FLUX_INGOT.get())
                .define('M', ModItems.MACHINE_FRAMES.get(MachineTier.T2).get())
                .unlockedBy("has_machine_frame_t2", has(ModItems.MACHINE_FRAMES.get(MachineTier.T2).get()))
                .save(writer);

        // --- Tier 1 machines ---
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.COMBUSTION_GENERATORS.get(MachineTier.T1).get())
                .pattern("III")
                .pattern("IMI")
                .pattern("CFC")
                .define('I', Items.IRON_INGOT)
                .define('M', ModItems.MACHINE_FRAMES.get(MachineTier.T1).get())
                .define('C', Items.COBBLESTONE)
                .define('F', Items.FURNACE)
                .unlockedBy("has_machine_frame_t1", has(ModItems.MACHINE_FRAMES.get(MachineTier.T1).get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ENERGY_CELLS.get(MachineTier.T1).get())
                .pattern("FRF")
                .pattern("RMR")
                .pattern("FRF")
                .define('F', ModItems.FLUX_INGOT.get())
                .define('R', Items.REDSTONE_BLOCK)
                .define('M', ModItems.MACHINE_FRAMES.get(MachineTier.T1).get())
                .unlockedBy("has_machine_frame_t1", has(ModItems.MACHINE_FRAMES.get(MachineTier.T1).get()))
                .save(writer);

        // --- Tier upgrades (fluxion:tier_upgrade — preserves machine NBT) ---
        addTierUpgrades(writer, ModItems.COMBUSTION_GENERATORS, "combustion_generator");
        addTierUpgrades(writer, ModItems.ENERGY_CELLS, "energy_cell");
    }

    private void addTierUpgrades(Consumer<FinishedRecipe> writer,
                                 Map<MachineTier, RegistryObject<Item>> family, String name) {
        for (MachineTier tier : new MachineTier[]{MachineTier.T2, MachineTier.T3}) {
            MachineTier previous = MachineTier.values()[tier.index() - 1];
            TierUpgradeRecipeBuilder.upgrade(family.get(previous).get(), family.get(tier).get())
                    .addIngredient(ModItems.MACHINE_FRAMES.get(tier).get())
                    .addIngredient(ModItems.FLUX_INGOT.get(), 4)
                    .save(writer, new ResourceLocation(Fluxion.MOD_ID,
                            name + "_" + tier.suffix() + "_upgrade"));
        }
    }
}
