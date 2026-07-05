package com.fluxion.registry;

import com.fluxion.Fluxion;
import com.fluxion.content.cell.EnergyCellBlock;
import com.fluxion.content.combustion.CombustionGeneratorBlock;
import com.fluxion.machine.MachineTier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Fluxion.MOD_ID);

    public static final Map<MachineTier, RegistryObject<CombustionGeneratorBlock>> COMBUSTION_GENERATORS =
            new EnumMap<>(MachineTier.class);
    public static final Map<MachineTier, RegistryObject<EnergyCellBlock>> ENERGY_CELLS =
            new EnumMap<>(MachineTier.class);

    static {
        for (MachineTier tier : MachineTier.values()) {
            COMBUSTION_GENERATORS.put(tier, BLOCKS.register(
                    "combustion_generator_" + tier.suffix(),
                    () -> new CombustionGeneratorBlock(machineProperties()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0), tier)));
        }
        for (MachineTier tier : MachineTier.values()) {
            ENERGY_CELLS.put(tier, BLOCKS.register(
                    "energy_cell_" + tier.suffix(),
                    () -> new EnergyCellBlock(machineProperties(), tier)));
        }
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private ModBlocks() {
    }
}
