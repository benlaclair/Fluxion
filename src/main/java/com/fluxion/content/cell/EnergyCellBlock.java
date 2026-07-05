package com.fluxion.content.cell;

import com.fluxion.machine.AbstractFluxMachineBlock;
import com.fluxion.machine.MachineTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Passive battery block. Accepts and provides FE on all sides; generators push
 * into it and pipes/machines pull out. (Per-side IO config arrives with the
 * tier-upgrade milestone.) No ticker — it does nothing on its own.
 */
public class EnergyCellBlock extends AbstractFluxMachineBlock {
    public EnergyCellBlock(Properties properties, MachineTier tier) {
        super(properties, tier);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCellBlockEntity(pos, state);
    }
}
