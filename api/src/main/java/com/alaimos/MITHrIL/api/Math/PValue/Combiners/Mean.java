package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class Mean implements CombinerInterface {

    @Override
    public String getName() {
        return "mean";
    }

    /**
     * Combine p-values by the mean p method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        Supplier<DoubleStream> d = () -> CombinerInterface.standardPValuesFilter(pValues, true, true);
        var k = d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        var z = (0.5 - d.get().average().orElse(0.0)) * Math.sqrt(12 * k);
        return 1 - (new NormalDistribution().cumulativeProbability(z));
    }
}
