package com.alaimos.MITHrIL.api.Math.PValue.Combiners.EmpiricalBrowns;

import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class EmpiricalDistributionTransformation implements Function<double[], double[]> {

    /**
     * Population variance
     *
     * @param x sample
     * @return population variance
     */
    private static double popVar(final double[] x) {
        return (new Variance().evaluate(x) * (x.length - 1)) / x.length;
    }

    /**
     * Population standard deviation
     *
     * @param x sample
     * @return population standard deviation
     */
    private static double popSd(final double[] x) {
        return Math.sqrt(popVar(x));
    }

    /**
     * Standardize a set of samples using population variance
     *
     * @param sample Sample to normalize.
     * @return standardized sample.
     */
    private static double @NotNull [] standardize(final double @NotNull [] sample) {
        var mean = new Mean().evaluate(sample);
        var sd = popSd(sample);
        var standardizedSample = new double[sample.length];
        for (var i = 0; i < sample.length; i++) {
            standardizedSample[i] = (sample[i] - mean) / sd;
        }
        return standardizedSample;
    }

    /**
     * Empirical cumulative distribution function
     *
     * @param data data
     * @return empirical cumulative distribution function
     */
    private static @NotNull EmpiricalDistribution ecdf(double[] data) {
        var distribution = new EmpiricalDistribution();
        distribution.load(data);
        return distribution;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param data the function argument
     * @return the function result
     */
    @Override
    public double[] apply(double[] data) {
        if (CombinerInterface.allEquals(data, 0.0)) return data.clone();
        data = standardize(data);
        var distribution = ecdf(data);
        for (var i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                data[i] = 0.0;
            } else {
                data[i] = -2 * Math.log(distribution.cumulativeProbability(data[i]));
            }
        }
        return data;
    }

}
