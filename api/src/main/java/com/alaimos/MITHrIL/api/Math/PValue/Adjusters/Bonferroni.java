package com.alaimos.MITHrIL.api.Math.PValue.Adjusters;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Default implementations of p-value adjusters
 */
public class Bonferroni implements AdjusterInterface {

    @Override
    public String name() {
        return "bonferroni";
    }

    @Override
    public String description() {
        return "Bonferroni method";
    }

    /**
     * Given a set of p-values, returns p-values adjusted using the Bonferroni method
     *
     * @param pValues a set of p-values
     * @return am array of corrected p-values
     */
    public double @NotNull [] adjust(double @NotNull ... pValues) {
        var n = pValues.length;
        if (n == 1) return new double[]{pValues[0]};
        return Arrays.stream(pValues).map(p -> Math.min(1, n * p)).toArray();
    }

}
