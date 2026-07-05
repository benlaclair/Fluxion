package com.fluxion.registry;

import com.fluxion.Fluxion;
import com.fluxion.content.cell.EnergyCellMenu;
import com.fluxion.content.combustion.CombustionGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Fluxion.MOD_ID);

    public static final RegistryObject<MenuType<CombustionGeneratorMenu>> COMBUSTION_GENERATOR =
            MENUS.register("combustion_generator",
                    () -> IForgeMenuType.create(CombustionGeneratorMenu::new));

    public static final RegistryObject<MenuType<EnergyCellMenu>> ENERGY_CELL =
            MENUS.register("energy_cell",
                    () -> IForgeMenuType.create(EnergyCellMenu::new));

    private ModMenus() {
    }
}
