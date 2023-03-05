package com.alaimos.MITHrIL.api.Math.PValue;

import com.alaimos.MITHrIL.api.Math.PValue.EmpiricalBrowns.EmpiricalBrownsMethod;
import com.alaimos.MITHrIL.api.Math.PValue.Interfaces.Combiner;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Default implementations of p-value combiners
 */
public class Combiners {

    /**
     * Returns a human-readable list of combiners
     *
     * @return an array of combiner names
     */
    public static String @NotNull [] getNames() {
        return new String[]{
                "Fisher",
                "Stouffer",
                "Mean",
                "Logit",
                "Wilkinson",
                "SumOfP",
                "VoteCounting",
                "ProductOfP"
        };
    }

    /**
     * Checks if all p-values have the same value
     *
     * @param pValues the p-values
     * @param value   a value
     * @return a boolean
     */
    public static boolean allEquals(double[] pValues, double value) {
        return Arrays.stream(pValues).allMatch(v -> v == value);
    }

    /**
     * Filters p-values in the range (0,1)
     *
     * @param pValues  an array of p-values
     * @param include0 include 0 values?
     * @param include1 include 1 values?
     * @return a stream of p-values
     */
    private static DoubleStream standardPValuesFilter(double[] pValues, boolean include0, boolean include1) {
        DoublePredicate p = (include0) ? v -> v >= 0 : v -> v > 0, p1 = (include1) ? v -> v <= 1 : v -> v < 1;
        return Arrays.stream(pValues).filter(p.and(p1));
    }

    /**
     * Returns a p-value combiner by its name
     *
     * @param name the name of a p-value combiner
     * @return the combiner
     */
    public static Combiner getByName(@NotNull String name) {
        return switch (name.toLowerCase()) {
            case "fisher" -> Combiners::fisher;
            case "mean" -> Combiners::mean;
            case "logit" -> Combiners::logit;
            case "wilkinson" -> Combiners.wilkinsonCombiner();
            case "sumofp" -> Combiners::sumOfP;
            case "votecounting" -> Combiners.voteCountingCombiner();
            case "productofp" -> Combiners::productOfP;
            case "browns" -> new EmpiricalBrownsMethod();
            default -> Combiners::stouffer;
        };
    }

    /**
     * Combine p-values by Fisher's method, that is the sum of logs method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double fisher(double... pValues) {
        if (allEquals(pValues, 0.0)) return 0.0;
        if (allEquals(pValues, 1.0)) return 1.0;
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, false, true);
        var count = d.get().count();
        if (count < 2) return d.get().findFirst().orElse(1.0);
        var chiSq = -2.0 * d.get().map(Math::log).sum();
        var df = 2 * count;
        return 1 - (new ChiSquaredDistribution(df).cumulativeProbability(chiSq));
    }

    /**
     * Combine p-values by Stouffer's method, that is the sum of z method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double stouffer(double... pValues) {
        if (allEquals(pValues, 0.0)) return 0.0;
        if (allEquals(pValues, 1.0)) return 1.0;
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, false, false);
        var count = d.get().count();
        if (count < 2) return d.get().findFirst().orElse(1.0);
        var n = new NormalDistribution();
        var zp = d.get().map(v -> n.inverseCumulativeProbability(1 - v)).sum() / Math.sqrt(count);
        return 1 - n.cumulativeProbability(zp);
    }

    /**
     * Combine p-values by the mean p method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double mean(double... pValues) {
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, true, true);
        var k = d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        var z = (0.5 - d.get().average().orElse(0.0)) * Math.sqrt(12 * k);
        return 1 - (new NormalDistribution().cumulativeProbability(z));
    }

    /**
     * Combine p-values using logit method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double logit(double... pValues) {
        if (allEquals(pValues, 0.0)) return 0.0;
        if (allEquals(pValues, 1.0)) return 1.0;
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, false, false);
        var k = d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        var psum = d.get().map(v -> Math.log(v / (1 - v))).sum();
        var mult = -1 / Math.sqrt(k * Math.pow(Math.PI, 2) * (5 * k + 2) / (3 * (5 * k + 4)));
        var t = mult * psum;
        var df = (5 * k + 4);
        return 1 - (new TDistribution(df).cumulativeProbability(t));
    }

    /**
     * Combine p-values using Wilkinson's method
     *
     * @param r       use the r-th minimum p-value
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double wilkinson(int r, double... pValues) {
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, true, true);
        var k = d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        Arrays.sort(pValues);
        if (r < 0 || r >= k) r = 1;
        var pr = pValues[r];
        return new BetaDistribution(r, k + 1 - r).cumulativeProbability(pr);
    }

    /**
     * Returns a p-value combiner which uses Wilkinson's method
     *
     * @return a p-values combiner
     */
    @NotNull
    @Contract(pure = true)
    public static Combiner wilkinsonCombiner() {
        return wilkinsonCombiner(1);
    }

    /**
     * Returns a p-value combiner which uses Wilkinson's method
     *
     * @param r use the r-th minimum p-value
     * @return a p-values combiner
     */
    @NotNull
    @Contract(pure = true)
    public static Combiner wilkinsonCombiner(int r) {
        return pv -> wilkinson(r, pv);
    }

    /**
     * Combine p-values using the sum p method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double sumOfP(double... pValues) {
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, true, true);
        var k = (int) d.get().count();
        if (k < 2) return d.get().findFirst().orElse(1.0);
        var pi = d.get().sum();
        var denom = CombinatoricsUtils.factorialLog(k);
        var nTerm = (int) Math.floor(pi) + 1;
        return IntStream.range(1, nTerm).mapToDouble(i -> (2 * (i % 2) - 1) *
                Math.exp(CombinatoricsUtils.binomialCoefficientLog(k, i - 1) + k * Math.log(pi - i + 1) - denom)).sum();
    }

    /**
     * Combine p-values using the product of p method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double productOfP(double... pValues) {
        return Arrays.stream(pValues).reduce(1, (a, b) -> a * b);
    }

    /**
     * Combine p-values by the vote counting method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    public static double voteCounting(double min, double max, double... pValues) {
        Supplier<DoubleStream> d = () -> standardPValuesFilter(pValues, true, true);
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

    /**
     * Returns a p-value combiner which uses Vote Counting method
     *
     * @return a p-values combiner
     */
    @NotNull
    @Contract(pure = true)
    public static Combiner voteCountingCombiner() {
        return voteCountingCombiner(0.5, 0.5);
    }

    /**
     * Returns a p-value combiner which uses Vote Counting method
     *
     * @param min the min value of the neutral zone
     * @param max the max value of the neutral zone
     * @return a p-values combiner
     */
    @NotNull
    @Contract(pure = true)
    public static Combiner voteCountingCombiner(double min, double max) {
        return pv -> voteCounting(min, max, pv);
    }


    /**
     * Convert two-sided p-value to one-sided
     *
     * @param p      a p-value
     * @param invert the p-value needs to be inverted
     * @return the one-sided p-value
     */
    @Contract(pure = true)
    public static double twoSidedToOneSided(double p, boolean invert) {
        return (invert) ? (1 - p) + p / 2 : p / 2;
    }

    /**
     * Convert two-sided p-values to one-sided
     *
     * @param p      p-values
     * @param invert which p-value needs to be inverted
     * @return the one-sided p-value
     */
    public static double[] twoSidedToOneSided(double @NotNull [] p, boolean[] invert) {
        return IntStream.range(0, p.length).mapToDouble(i -> twoSidedToOneSided(p[i], invert[i])).toArray();
    }

}
