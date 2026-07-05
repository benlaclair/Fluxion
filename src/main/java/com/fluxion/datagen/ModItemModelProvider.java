package com.fluxion.datagen;

import com.fluxion.Fluxion;
import com.fluxion.machine.MachineTier;
import com.fluxion.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Fluxion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.FLUX_INGOT.get());
        for (MachineTier tier : MachineTier.values()) {
            basicItem(ModItems.MACHINE_FRAMES.get(tier).get());
        }
        // Machine block items are generated alongside their block models.
    }
}
