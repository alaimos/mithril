package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class Fisher implements CombinerInterface {

    @Override
    public String name() {
        return "fisher";
    }

    /**
     * Combine p-values by Fisher's method, that is the sum of logs method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        if (CombinerInterface.allEquals(pValues, 0.0)) return 0.0;
        if (CombinerInterface.allEquals(pValues, 1.0)) return 1.0;
        Supplier<DoubleStream> d = () -> CombinerInterface.standardPValuesFilter(pValues, false, true);
        var count = d.get().count();
        if (count < 2) return d.get().findFirst().orElse(1.0);
        var chiSq = -2.0 * d.get().map(Math::log).sum();
        var df = 2 * count;
        return 1 - (new ChiSquaredDistribution(df).cumulativeProbability(chiSq));
    }
}
