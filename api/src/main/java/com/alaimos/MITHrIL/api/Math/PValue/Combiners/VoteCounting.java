package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;

public class VoteCounting implements CombinerInterface {

    private double min = 0.5;
    private double max = 0.5;

    @Override
    public String name() {
        return "vote.counting";
    }

    public double getMin() {
        return min;
    }

    public VoteCounting setMin(double min) {
        this.min = min;
        return this;
    }

    public double getMax() {
        return max;
    }

    public VoteCounting setMax(double max) {
        this.max = max;
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
        var k = (int) d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        if (min <= 0 || min >= 1 || max <= 0 || max >= 1) {
            throw new RuntimeException("Min and max parameters are out of range");
        }
        var pos = (int) d.get().filter(v -> v < min).count();
        var neg = (int) d.get().filter(v -> v > max).count();
        if ((pos + neg) <= 0) return 1.0;
        return new BinomialTest().binomialTest(pos + neg, pos, 0.5, AlternativeHypothesis.GREATER_THAN);
    }
}
