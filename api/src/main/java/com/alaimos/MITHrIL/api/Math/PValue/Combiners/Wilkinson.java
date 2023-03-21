package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class Wilkinson implements CombinerInterface {

    private int r = 1;

    @Override
    public String name() {
        return "wilkinson";
    }

    @Override
    public String description() {
        return "Wilkinson's method";
    }

    public int getR() {
        return r;
    }

    public Wilkinson setR(int r) {
        this.r = r;
        return this;
    }

    /**
     * Combine p-values using Wilkinson's method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        Supplier<DoubleStream> d = () -> CombinerInterface.standardPValuesFilter(pValues, true, true);
        var k = d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        Arrays.sort(pValues);
        if (r < 0 || r >= k) r = 1;
        var pr = pValues[r];
        return new BetaDistribution(r, k + 1 - r).cumulativeProbability(pr);
    }
}
