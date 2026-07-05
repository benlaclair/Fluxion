package com.fluxion.energy;

import net.minecraftforge.energy.EnergyStorage;

/**
 * Forge {@link EnergyStorage} with a change callback (for {@code setChanged()})
 * and internal mutators that bypass the external receive/extract limits —
 * generators insert their own output even when {@code maxReceive} is 0.
 */
public class ModEnergyStorage extends EnergyStorage {
    private final Runnable onChanged;

    public ModEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract);
        this.onChanged = onChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received != 0 && !simulate) {
            onChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted != 0 && !simulate) {
            onChanged.run();
        }
        return extracted;
    }

    /** Internal insertion, ignoring {@code maxReceive}. Returns the amount actually added. */
    public int addEnergy(int amount) {
        int added = Math.min(amount, capacity - energy);
        if (added > 0) {
            energy += added;
            onChanged.run();
        }
        return added;
    }

    /** Internal removal, ignoring {@code maxExtract}. Returns the amount actually removed. */
    public int consumeEnergy(int amount) {
        int removed = Math.min(amount, energy);
        if (removed > 0) {
            energy -= removed;
            onChanged.run();
        }
        return removed;
    }

    /** Direct set for NBT loading; clamps to capacity and does not fire the change callback. */
    public void setEnergy(int amount) {
        this.energy = Math.max(0, Math.min(amount, capacity));
    }
}
