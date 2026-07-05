package com.fluxion.machine;

/**
 * Machine tier ladder. Each machine family registers one block per tier
 * (e.g. {@code fluxion:combustion_generator_t1}) and looks up its balance
 * numbers from {@link com.fluxion.FluxionConfig} by {@link #index()}.
 */
public enum MachineTier {
    T1,
    T2,
    T3;

    /** Zero-based index used for config array lookups. */
    public int index() {
        return ordinal();
    }

    /** Registry-name suffix, e.g. {@code "t1"}. */
    public String suffix() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Display number, 1-based. */
    public int number() {
        return ordinal() + 1;
    }
}
