package com.fluxion.datagen;

import com.fluxion.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;

public final class ModLootTableProvider {
    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)));
    }

    public static class ModBlockLootTables extends BlockLootSubProvider {
        protected ModBlockLootTables() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            // Machines keep their stored energy in the dropped item ("Energy" is
            // copied into BlockEntityTag, which vanilla BlockItem restores on
            // placement). Fuel/inventories spill on break instead.
            ModBlocks.BLOCKS.getEntries().forEach(block -> machineDrop(block.get()));
        }

        private void machineDrop(Block block) {
            add(block, LootTable.lootTable().withPool(
                    LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(applyExplosionCondition(block, LootItem.lootTableItem(block)
                                    .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                            .copy("Energy", "BlockEntityTag.Energy"))))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
        }
    }

    private ModLootTableProvider() {
    }
}
