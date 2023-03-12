package com.alaimos.MITHrIL.api.Math.PValue.Adjusters;

import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

import static com.alaimos.MITHrIL.api.Math.Vectors.*;

/**
 * Default implementations of p-value adjusters
 */
public class BenjaminiYekutieli implements AdjusterInterface {

    @Override
    public String name() {
        return "benjamini.yekutieli";
    }

    /**
     * Given a set of p-values, returns p-values adjusted using Benjamini & Yekutieli (2001) method
     *
     * @param pValues a set of p-values
     * @return am array of corrected p-values
     */
    public double @NotNull [] adjust(double @NotNull ... pValues) {
        if (pValues.length == 1) return new double[]{pValues[0]};
        var n = pValues.length;
        var q = IntStream.range(0, n).mapToDouble(i -> 1 / (((double) i) + 1)).sum();
        var o = decreasingOrder(pValues);
        var ro = order(o);
        var po = sortFromIndex(pValues, o);
        var pp = IntStream.range(0, n).mapToDouble(i -> (q * n * po[i]) / (n - i)).toArray();
        return sortFromIndex(parallelMin(cumulativeMin(pp), 1), ro);
    }

}
