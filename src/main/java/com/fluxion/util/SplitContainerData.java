package com.fluxion.util;

import net.minecraft.world.inventory.ContainerData;

/**
 * Vanilla's {@code ClientboundContainerSetDataPacket} truncates each synced value
 * to a signed short (16 bits), which corrupts FE amounts above 32k. This wrapper
 * exposes each logical int as two 16-bit raw slots so {@code addDataSlots} can
 * sync full 32-bit values.
 *
 * <p>Server side: wrap a {@link ContainerData} backed by the block entity.
 * Client side: wrap a {@code SimpleContainerData} of the same logical size; the
 * split halves are recombined into it as they arrive. Read full values with
 * {@link #getCombined(int)} on either side.
 */
public class SplitContainerData implements ContainerData {
    private final ContainerData wrapped;

    public SplitContainerData(ContainerData wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int get(int index) {
        int value = wrapped.get(index >> 1);
        return (index & 1) == 0 ? value & 0xFFFF : (value >>> 16) & 0xFFFF;
    }

    @Override
    public void set(int index, int value) {
        int current = wrapped.get(index >> 1);
        if ((index & 1) == 0) {
            wrapped.set(index >> 1, (current & 0xFFFF0000) | (value & 0xFFFF));
        } else {
            wrapped.set(index >> 1, ((value & 0xFFFF) << 16) | (current & 0xFFFF));
        }
    }

    @Override
    public int getCount() {
        return wrapped.getCount() * 2;
    }

    /** Full 32-bit value at the logical (pre-split) index. */
    public int getCombined(int logicalIndex) {
        return wrapped.get(logicalIndex);
    }
}
