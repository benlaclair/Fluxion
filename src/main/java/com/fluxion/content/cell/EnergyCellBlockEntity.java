package com.fluxion.content.cell;

import com.fluxion.FluxionConfig;
import com.fluxion.energy.ModEnergyStorage;
import com.fluxion.machine.AbstractFluxMachineBE;
import com.fluxion.machine.MachineTier;
import com.fluxion.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnergyCellBlockEntity extends AbstractFluxMachineBE implements MenuProvider {
    public EnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CELL.get(), pos, state, tierOf(state));
    }

    private static MachineTier tierOf(BlockState state) {
        return ((EnergyCellBlock) state.getBlock()).getTier();
    }

    @Override
    protected ModEnergyStorage createEnergyStorage() {
        int transfer = FluxionConfig.ENERGY_CELL_TRANSFER_RATE[tier.index()].get();
        return new ModEnergyStorage(
                FluxionConfig.ENERGY_CELL_CAPACITY[tier.index()].get(),
                transfer,
                transfer,
                this::setChanged);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EnergyCellMenu(containerId, playerInventory, this);
    }
}
