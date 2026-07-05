package com.fluxion.datagen;

import com.fluxion.Fluxion;
import com.fluxion.content.combustion.CombustionGeneratorBlock;
import com.fluxion.machine.MachineTier;
import com.fluxion.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Fluxion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (MachineTier tier : MachineTier.values()) {
            Block block = ModBlocks.COMBUSTION_GENERATORS.get(tier).get();
            String name = "combustion_generator_" + tier.suffix();

            ModelFile off = models().orientable(name,
                    modLoc("block/" + name + "_side"),
                    modLoc("block/" + name + "_front"),
                    modLoc("block/" + name + "_top"));
            ModelFile on = models().orientable(name + "_on",
                    modLoc("block/" + name + "_side"),
                    modLoc("block/" + name + "_front_on"),
                    modLoc("block/" + name + "_top"));

            horizontalBlock(block, state -> state.getValue(CombustionGeneratorBlock.LIT) ? on : off);
            simpleBlockItem(block, off);
        }

        for (MachineTier tier : MachineTier.values()) {
            Block block = ModBlocks.ENERGY_CELLS.get(tier).get();
            String name = "energy_cell_" + tier.suffix();
            simpleBlockWithItem(block, models().cubeAll(name, modLoc("block/" + name)));
        }
    }
}
