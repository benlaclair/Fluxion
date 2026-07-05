package com.fluxion.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Base block for all Fluxion machines: opens the BE-provided menu on use,
 * forwards neighbor changes to the BE's capability cache, and drops inventory
 * contents on removal.
 */
public abstract class AbstractFluxMachineBlock extends BaseEntityBlock {
    protected final MachineTier tier;

    protected AbstractFluxMachineBlock(Properties properties, MachineTier tier) {
        super(properties);
        this.tier = tier;
    }

    public MachineTier getTier() {
        return tier;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            NetworkHooks.openScreen((ServerPlayer) player, provider, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AbstractFluxMachineBE machine) {
            machine.onNeighborChanged(neighborPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof AbstractFluxMachineBE machine) {
            machine.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
