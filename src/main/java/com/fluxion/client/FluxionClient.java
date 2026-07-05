package com.fluxion.client;

import com.fluxion.Fluxion;
import com.fluxion.content.cell.EnergyCellScreen;
import com.fluxion.content.combustion.CombustionGeneratorScreen;
import com.fluxion.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Fluxion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FluxionClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.COMBUSTION_GENERATOR.get(), CombustionGeneratorScreen::new);
            MenuScreens.register(ModMenus.ENERGY_CELL.get(), EnergyCellScreen::new);
        });
    }

    private FluxionClient() {
    }
}
