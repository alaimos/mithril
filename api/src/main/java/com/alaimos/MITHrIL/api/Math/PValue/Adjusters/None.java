package com.alaimos.MITHrIL.api.Math.PValue.Adjusters;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Default implementations of p-value adjusters
 */
public class None implements AdjusterInterface {

    @Override
    public String name() {
        return "none";
    }

    @Override
    public String description() {
        return "No adjustment";
    }

    /**
     * Given a set of p-values, returns p-values adjusted using Benjamini & Yekutieli (2001) method
     *
     * @param pValues a set of p-values
     * @return am array of corrected p-values
     */
    public double @NotNull [] adjust(double @NotNull ... pValues) {
        return Arrays.copyOf(pValues, pValues.length);
    }

}
