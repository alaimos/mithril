package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class Stouffer implements CombinerInterface {

    @Override
    public String name() {
        return "stouffer";
    }

    @Override
    public String description() {
        return "Stouffer's method";
    }

    /**
     * Combine p-values by Stouffer's method, that is the sum of z method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        if (CombinerInterface.allEquals(pValues, 0.0)) return 0.0;
        if (CombinerInterface.allEquals(pValues, 1.0)) return 1.0;
        Supplier<DoubleStream> d = () -> CombinerInterface.standardPValuesFilter(pValues, false, false);
        var count = d.get().count();
        if (count < 2) return d.get().findFirst().orElse(1.0);
        var n = new NormalDistribution();
        var zp = d.get().map(v -> n.inverseCumulativeProbability(1 - v)).sum() / Math.sqrt(count);
        return 1 - n.cumulativeProbability(zp);
    }
}
