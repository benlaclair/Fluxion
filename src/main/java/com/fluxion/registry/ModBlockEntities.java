package com.fluxion.registry;

import com.fluxion.Fluxion;
import com.fluxion.content.cell.EnergyCellBlockEntity;
import com.fluxion.content.combustion.CombustionGeneratorBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Fluxion.MOD_ID);

    // One BE type per machine family; all tier blocks are valid for it.
    public static final RegistryObject<BlockEntityType<CombustionGeneratorBlockEntity>> COMBUSTION_GENERATOR =
            BLOCK_ENTITIES.register("combustion_generator", () -> BlockEntityType.Builder.of(
                    CombustionGeneratorBlockEntity::new,
                    ModBlocks.COMBUSTION_GENERATORS.values().stream()
                            .map(RegistryObject::get).toArray(Block[]::new)
            ).build(null));

    public static final RegistryObject<BlockEntityType<EnergyCellBlockEntity>> ENERGY_CELL =
            BLOCK_ENTITIES.register("energy_cell", () -> BlockEntityType.Builder.of(
                    EnergyCellBlockEntity::new,
                    ModBlocks.ENERGY_CELLS.values().stream()
                            .map(RegistryObject::get).toArray(Block[]::new)
            ).build(null));

    private ModBlockEntities() {
    }
}
