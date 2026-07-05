package com.fluxion;

import com.fluxion.registry.ModBlockEntities;
import com.fluxion.registry.ModBlocks;
import com.fluxion.registry.ModCreativeTabs;
import com.fluxion.registry.ModItems;
import com.fluxion.registry.ModMenus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Fluxion.MOD_ID)
public class Fluxion {
    public static final String MOD_ID = "fluxion";

    public Fluxion() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        // SERVER config: per-world, synced to remote clients on login. All balance
        // numbers (FE/t, buffers, transfer rates, efficiency) live here — never hardcoded.
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, FluxionConfig.SPEC);
    }
}
