package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class SumOfP implements CombinerInterface {

    @Override
    public String name() {
        return "sum.of.p";
    }

    @Override
    public String description() {
        return "Sum of p-values method";
    }

    /**
     * Combine p-values using the sum p method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        Supplier<DoubleStream> d = () -> CombinerInterface.standardPValuesFilter(pValues, true, true);
        var k = (int) d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        var pi = d.get().sum();
        var denom = CombinatoricsUtils.factorialLog(k);
        var nTerm = (int) Math.floor(pi) + 1;
        return IntStream
                .range(1, nTerm)
                .mapToDouble(i -> (2 * (i % 2) - 1) * Math.exp(CombinatoricsUtils.binomialCoefficientLog(k, i - 1) + k * Math.log(pi - i + 1) - denom))
                .sum();
    }
}
