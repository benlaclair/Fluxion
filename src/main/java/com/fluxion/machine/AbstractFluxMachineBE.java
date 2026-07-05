package com.fluxion.machine;

import com.fluxion.energy.ModEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all Fluxion machines: FE storage, capability caching with
 * invalidation, and NBT round-tripping. Neighbor energy capabilities are cached
 * and only refreshed when the neighbor changes (no per-tick lookups).
 */
public abstract class AbstractFluxMachineBE extends BlockEntity {
    protected final MachineTier tier;
    protected final ModEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyCap;

    @SuppressWarnings("unchecked")
    private final LazyOptional<IEnergyStorage>[] neighborEnergyCache = new LazyOptional[Direction.values().length];

    protected AbstractFluxMachineBE(BlockEntityType<?> type, BlockPos pos, BlockState state, MachineTier tier) {
        super(type, pos, state);
        this.tier = tier;
        this.energy = createEnergyStorage();
        this.energyCap = LazyOptional.of(() -> energy);
    }

    /**
     * Called once from the constructor; read capacity/rates from config using
     * {@link #tier}. Do not touch subclass fields here — they are not
     * initialized yet.
     */
    protected abstract ModEnergyStorage createEnergyStorage();

    public MachineTier getTier() {
        return tier;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    /**
     * Pushes up to {@code maxPerSide} FE into each adjacent energy receiver,
     * limited by this machine's own {@code maxExtract}.
     */
    protected void pushEnergy(int maxPerSide) {
        for (Direction dir : Direction.values()) {
            if (energy.getEnergyStored() <= 0) {
                return;
            }
            IEnergyStorage target = getNeighborEnergy(dir);
            if (target == null || !target.canReceive()) {
                continue;
            }
            int available = energy.extractEnergy(maxPerSide, true);
            if (available <= 0) {
                return;
            }
            int accepted = target.receiveEnergy(available, false);
            if (accepted > 0) {
                energy.extractEnergy(accepted, false);
            }
        }
    }

    @Nullable
    private IEnergyStorage getNeighborEnergy(Direction dir) {
        int idx = dir.get3DDataValue();
        LazyOptional<IEnergyStorage> cached = neighborEnergyCache[idx];
        if (cached == null) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
            if (neighbor != null) {
                cached = neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite());
                cached.addListener(opt -> neighborEnergyCache[idx] = null);
            } else {
                cached = LazyOptional.empty();
            }
            neighborEnergyCache[idx] = cached;
        }
        return cached.resolve().orElse(null);
    }

    /** Called from the block when an adjacent block changes; drops the stale cache entry. */
    public void onNeighborChanged(BlockPos neighborPos) {
        for (Direction dir : Direction.values()) {
            if (worldPosition.relative(dir).equals(neighborPos)) {
                neighborEnergyCache[dir.get3DDataValue()] = null;
                return;
            }
        }
    }

    /** Drop inventory contents on block removal. Default: nothing to drop. */
    public void dropContents(Level level, BlockPos pos) {
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergy(tag.getInt("Energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Energy", energy.getEnergyStored());
    }
}
