package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.pf4j.ExtensionPoint;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A function which combines input p-values
 */
@FunctionalInterface
public interface CombinerInterface extends ExtensionPoint {

    /**
     * Checks if all p-values have the same value
     *
     * @param pValues the p-values
     * @param value   a value
     * @return a boolean
     */
    static boolean allEquals(double @NotNull [] pValues, double value) {
        for (double pValue : pValues) {
            if (pValue != value) {
                return false;
            }
        }
        return true;
    }

    /**
     * Filters p-values in the range (0,1)
     *
     * @param pValues  an array of p-values
     * @param include0 include 0 values?
     * @param include1 include 1 values?
     * @return a stream of p-values
     */
    static DoubleStream standardPValuesFilter(double[] pValues, boolean include0, boolean include1) {
        DoublePredicate p = (include0) ? v -> v >= 0 : v -> v > 0, p1 = (include1) ? v -> v <= 1 : v -> v < 1;
        return Arrays.stream(pValues).filter(p.and(p1));
    }

    /**
     * Convert two-sided p-value to one-sided
     *
     * @param p      a p-value
     * @param invert the p-value needs to be inverted
     * @return the one-sided p-value
     */
    @Contract(pure = true)
    static double twoSidedToOneSided(double p, boolean invert) {
        return (invert) ? (1 - p) + p / 2 : p / 2;
    }

    /**
     * Convert two-sided p-values to one-sided
     *
     * @param p      p-values
     * @param invert which p-value needs to be inverted
     * @return the one-sided p-value
     */
    static double[] twoSidedToOneSided(double @NotNull [] p, boolean[] invert) {
        return IntStream.range(0, p.length).mapToDouble(i -> twoSidedToOneSided(p[i], invert[i])).toArray();
    }

    /**
     * Returns the name of the combiner
     *
     * @return the name of the combiner
     */
    default String name() {
        return "unknown";
    }

    /**
     * Combines p-values
     *
     * @param pValues p-values to combine
     * @return combined p-value
     */
    double combine(double... pValues);

}
