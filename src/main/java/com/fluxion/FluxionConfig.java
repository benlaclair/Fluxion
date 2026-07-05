package com.fluxion;

import com.fluxion.machine.MachineTier;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * All balance numbers for the mod. Values are indexed by {@link MachineTier#index()}.
 *
 * <p>Registered as a SERVER config: stored per-world under {@code serverconfig/} and
 * synced to clients when they join, so GUIs and logic always agree.
 */
public final class FluxionConfig {
    public static final ForgeConfigSpec SPEC;

    // --- Combustion Generator ---
    public static final ForgeConfigSpec.IntValue[] COMBUSTION_FE_PER_TICK = new ForgeConfigSpec.IntValue[3];
    public static final ForgeConfigSpec.IntValue[] COMBUSTION_BUFFER = new ForgeConfigSpec.IntValue[3];
    public static final ForgeConfigSpec.IntValue[] COMBUSTION_TRANSFER_RATE = new ForgeConfigSpec.IntValue[3];
    public static final ForgeConfigSpec.DoubleValue[] COMBUSTION_FUEL_EFFICIENCY = new ForgeConfigSpec.DoubleValue[3];

    // --- Energy Cell ---
    public static final ForgeConfigSpec.IntValue[] ENERGY_CELL_CAPACITY = new ForgeConfigSpec.IntValue[3];
    public static final ForgeConfigSpec.IntValue[] ENERGY_CELL_TRANSFER_RATE = new ForgeConfigSpec.IntValue[3];

    private static final int[] COMBUSTION_FE_DEFAULTS = {40, 160, 640};
    private static final int[] COMBUSTION_BUFFER_DEFAULTS = {100_000, 400_000, 1_600_000};
    private static final int[] COMBUSTION_TRANSFER_DEFAULTS = {160, 640, 2_560};
    private static final double[] COMBUSTION_EFFICIENCY_DEFAULTS = {1.0, 1.25, 1.5};

    private static final int[] CELL_CAPACITY_DEFAULTS = {500_000, 2_000_000, 8_000_000};
    private static final int[] CELL_TRANSFER_DEFAULTS = {1_000, 4_000, 16_000};

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Combustion Generator — burns furnace fuels for FE").push("combustion_generator");
        for (MachineTier tier : MachineTier.values()) {
            int i = tier.index();
            builder.push(tier.suffix());
            COMBUSTION_FE_PER_TICK[i] = builder
                    .comment("FE generated per tick while burning")
                    .defineInRange("fePerTick", COMBUSTION_FE_DEFAULTS[i], 1, Integer.MAX_VALUE);
            COMBUSTION_BUFFER[i] = builder
                    .comment("Internal energy buffer size (FE)")
                    .defineInRange("buffer", COMBUSTION_BUFFER_DEFAULTS[i], 1, Integer.MAX_VALUE);
            COMBUSTION_TRANSFER_RATE[i] = builder
                    .comment("Max FE pushed to each adjacent block per tick")
                    .defineInRange("transferRate", COMBUSTION_TRANSFER_DEFAULTS[i], 1, Integer.MAX_VALUE);
            COMBUSTION_FUEL_EFFICIENCY[i] = builder
                    .comment("Burn time multiplier (1.25 = items burn 25% longer)")
                    .defineInRange("fuelEfficiency", COMBUSTION_EFFICIENCY_DEFAULTS[i], 0.1, 100.0);
            builder.pop();
        }
        builder.pop();

        builder.comment("Energy Cell — battery block").push("energy_cell");
        for (MachineTier tier : MachineTier.values()) {
            int i = tier.index();
            builder.push(tier.suffix());
            ENERGY_CELL_CAPACITY[i] = builder
                    .comment("Energy capacity (FE)")
                    .defineInRange("capacity", CELL_CAPACITY_DEFAULTS[i], 1, Integer.MAX_VALUE);
            ENERGY_CELL_TRANSFER_RATE[i] = builder
                    .comment("Max FE received/extracted per tick (per side)")
                    .defineInRange("transferRate", CELL_TRANSFER_DEFAULTS[i], 1, Integer.MAX_VALUE);
            builder.pop();
        }
        builder.pop();

        SPEC = builder.build();
    }

    private FluxionConfig() {
    }
}
