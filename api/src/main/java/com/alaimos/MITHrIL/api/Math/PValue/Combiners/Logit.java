package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.distribution.TDistribution;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class Logit implements CombinerInterface {

    @Override
    public String name() {
        return "logit";
    }

    @Override
    public String description() {
        return "Logit method";
    }

    /**
     * Combine p-values using logit method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        if (CombinerInterface.allEquals(pValues, 0.0)) return 0.0;
        if (CombinerInterface.allEquals(pValues, 1.0)) return 1.0;
        Supplier<DoubleStream> d = () -> CombinerInterface.standardPValuesFilter(pValues, false, false);
        var k = d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        var psum = d.get().map(v -> Math.log(v / (1 - v))).sum();
        var mult = -1 / Math.sqrt(k * Math.pow(Math.PI, 2) * (5 * k + 2) / (3 * (5 * k + 4)));
        var t = mult * psum;
        var df = (5 * k + 4);
        return 1 - (new TDistribution(df).cumulativeProbability(t));
    }
}
