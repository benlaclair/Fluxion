package com.fluxion.datagen;

import com.fluxion.Fluxion;
import com.fluxion.machine.MachineTier;
import com.fluxion.registry.ModBlocks;
import com.fluxion.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, Fluxion.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + Fluxion.MOD_ID, "Fluxion");

        add(ModItems.FLUX_INGOT.get(), "Flux Ingot");
        for (MachineTier tier : MachineTier.values()) {
            add(ModItems.MACHINE_FRAMES.get(tier).get(), "Machine Frame (Tier " + tier.number() + ")");
            add(ModBlocks.COMBUSTION_GENERATORS.get(tier).get(), "Combustion Generator (Tier " + tier.number() + ")");
            add(ModBlocks.ENERGY_CELLS.get(tier).get(), "Energy Cell (Tier " + tier.number() + ")");
        }

        add("gui.fluxion.energy", "%s / %s FE");
        add("tooltip.fluxion.stored_energy", "Stored: %s FE");
    }
}
