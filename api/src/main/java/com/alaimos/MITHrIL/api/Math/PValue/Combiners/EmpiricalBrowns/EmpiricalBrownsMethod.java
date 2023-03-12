package com.alaimos.MITHrIL.api.Math.PValue.Combiners.EmpiricalBrowns;

import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Empirical Brown's method
 */
public class EmpiricalBrownsMethod implements CombinerInterface {

    private static DataMatrix defaultDataMatrix;

    private static String[] filter;

    public static DataMatrix getDefaultDataMatrix() {
        return defaultDataMatrix;
    }

    public static void setDefaultDataMatrix(@NotNull DataMatrix defaultDataMatrix) {
        EmpiricalBrownsMethod.defaultDataMatrix = defaultDataMatrix;
        defaultDataMatrix.transform(new EmpiricalDistributionTransformation());
    }

    public static String[] getFilter() {
        return filter;
    }

    public static void setFilter(String[] filter) {
        EmpiricalBrownsMethod.filter = filter;
    }

    public static @NotNull Pair<String[], double[]> filter(double @NotNull [] pValues) {
        var indices = defaultDataMatrix.rowIndicesByNames(filter);
        var keep = new ArrayList<Integer>();
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] >= 0 && pValues[i] > 0 && pValues[i] <= 1) {
                keep.add(i);
            }
        }
        var keepFilter = new String[keep.size()];
        var keepPValues = new double[keep.size()];
        for (var i = 0; i < keep.size(); i++) {
            keepFilter[i] = filter[keep.get(i)];
            keepPValues[i] = pValues[keep.get(i)];
        }
        return Pair.of(keepFilter, keepPValues);
    }

    @Override
    public String name() {
        return "empirical.browns";
    }

    /**
     * Combine p-values using the empirical Brown's method
     *
     * @param pValues p-values to combine
     * @return the combined p-value
     */
    @Override
    public double combine(double... pValues) {
        var tmp = filter(pValues);
        var filter = tmp.getLeft();
        pValues = tmp.getRight();
        if (filter.length != pValues.length) {
            throw new IllegalArgumentException("The number of p-values should be the same as the length of the filter");
        }
        if (pValues.length == 0) return 1.0;
        if (pValues.length == 1) return pValues[0];
        var sm = defaultDataMatrix.subMatrix(filter);
        var dm = sm.computeCovarianceMatrix();
        var N = dm.cols();
        var dfFisher = 2.0 * N;
        var expected = 2.0 * N;
        var covSum = 2.0 * Arrays.stream(dm.lowerTriangular(false)).filter(d -> !Double.isNaN(d)).sum();
        var var = 4 * N + covSum;
        var c = var / (2.0 * expected);
        var dfBrown = (2.0 * Math.pow(expected, 2)) / var;
        if (dfBrown > dfFisher) {
            dfBrown = dfFisher;
            c = 1.0;
        }
        var x = 2.0 * Arrays.stream(pValues).map(p -> -Math.log(p)).sum();
        return 1.0 - new ChiSquaredDistribution(dfBrown).cumulativeProbability(x / c);
    }
}
