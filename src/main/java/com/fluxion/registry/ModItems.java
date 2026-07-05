package com.fluxion.registry;

import com.fluxion.Fluxion;
import com.fluxion.machine.MachineTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Fluxion.MOD_ID);

    // Materials (registration order = creative tab order)
    public static final RegistryObject<Item> FLUX_INGOT =
            ITEMS.register("flux_ingot", () -> new Item(new Item.Properties()));

    public static final Map<MachineTier, RegistryObject<Item>> MACHINE_FRAMES =
            new EnumMap<>(MachineTier.class);

    // Block items
    public static final Map<MachineTier, RegistryObject<Item>> COMBUSTION_GENERATORS =
            new EnumMap<>(MachineTier.class);
    public static final Map<MachineTier, RegistryObject<Item>> ENERGY_CELLS =
            new EnumMap<>(MachineTier.class);

    static {
        for (MachineTier tier : MachineTier.values()) {
            MACHINE_FRAMES.put(tier, ITEMS.register(
                    "machine_frame_" + tier.suffix(),
                    () -> new Item(new Item.Properties())));
        }
        for (MachineTier tier : MachineTier.values()) {
            COMBUSTION_GENERATORS.put(tier, ITEMS.register(
                    "combustion_generator_" + tier.suffix(),
                    () -> new BlockItem(ModBlocks.COMBUSTION_GENERATORS.get(tier).get(), new Item.Properties())));
        }
        for (MachineTier tier : MachineTier.values()) {
            ENERGY_CELLS.put(tier, ITEMS.register(
                    "energy_cell_" + tier.suffix(),
                    () -> new BlockItem(ModBlocks.ENERGY_CELLS.get(tier).get(), new Item.Properties())));
        }
    }

    private ModItems() {
    }
}
