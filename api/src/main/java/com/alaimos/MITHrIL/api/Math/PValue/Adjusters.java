package com.alaimos.MITHrIL.api.Math.PValue;

import com.alaimos.MITHrIL.api.Math.PValue.Interfaces.Adjuster;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.IntStream;

import static com.alaimos.MITHrIL.api.Math.Vectors.*;

/**
 * Default implementations of p-value adjusters
 */
public class Adjusters {

    /**
     * Returns a human-readable list of adjusters
     *
     * @return an array of adjuster names
     */
    public static String @NotNull [] getNames() {
        return new String[]{
                "bonferroni",
                "holm",
                "hochberg",
                "BY (Benjamini & Yekutieli)",
                "BH (Benjamini & Hochberg)",
                "None"
        };
    }

    /**
     * Returns a p-value adjuster by its name
     *
     * @param name the name of a p-value adjuster
     * @return an adjuster
     */
    @Contract(pure = true)
    public static Adjuster getByName(@NotNull String name) {
        return switch (name.toLowerCase()) {
            case "bonferroni" -> Adjusters::bonferroni;
            case "holm" -> Adjusters::holm;
            case "hochberg" -> Adjusters::hochberg;
            case "by", "benjaminiyekutieli" -> Adjusters::benjaminiYekutieli;
            case "none" -> Adjusters::none;
            default -> Adjusters::benjaminiHochberg;
        };
    }

    /**
     * Given a set of p-values, returns p-values adjusted using the Bonferroni method
     *
     * @param pValues a set of p-values
     * @return am array of corrected p-values
     */
    public static double @NotNull [] bonferroni(double @NotNull ... pValues) {
        var n = pValues.length;
        if (n == 1) return new double[]{pValues[0]};
        return Arrays.stream(pValues).map(p -> Math.min(1, n * p)).toArray();
    }

    /**
     * Given a set of p-values, returns p-values adjusted using the Holm (1979) method
     *
     * @param pValues a set of p-values
     * @return an array of corrected p-values
     */
    public static double @NotNull [] holm(double @NotNull ... pValues) {
        if (pValues.length == 1) return new double[]{pValues[0]};
        var n = pValues.length;
        var o = order(pValues);
        var ro = order(o);
        var po = sortFromIndex(pValues, o);
        var pp = IntStream.range(0, n).mapToDouble(i -> (n - i) * po[i]).toArray();
        return sortFromIndex(parallelMin(cumulativeMax(pp), 1), ro);
    }

    /**
     * Given a set of p-values, returns p-values adjusted using the Hochberg (1988) method
     *
     * @param pValues a set of p-values
     * @return an array of corrected p-values
     */
    public static double @NotNull [] hochberg(double @NotNull ... pValues) {
        if (pValues.length == 1) return new double[]{pValues[0]};
        var n = pValues.length;
        var o = decreasingOrder(pValues);
        var ro = order(o);
        var po = sortFromIndex(pValues, o);
        var pp = IntStream.range(0, n).mapToDouble(i -> (i + 1) * po[i]).toArray();
        return sortFromIndex(parallelMin(cumulativeMin(pp), 1), ro);
    }

    /**
     * Given a set of p-values, returns p-values adjusted using the Benjamini & Hochberg (1995) method
     *
     * @param pValues a set of p-values
     * @return an array of corrected p-values
     */
    public static double @NotNull [] benjaminiHochberg(double @NotNull ... pValues) {
        if (pValues.length == 1) return new double[]{pValues[0]};
        var n = pValues.length;
        var o = decreasingOrder(pValues);
        var ro = order(o);
        var po = sortFromIndex(pValues, o);
        var pp = IntStream.range(0, n).mapToDouble(i -> (n * po[i]) / (n - i)).toArray();
        return sortFromIndex(parallelMin(cumulativeMin(pp), 1), ro);
    }

    /**
     * Given a set of p-values, returns p-values adjusted using Benjamini & Yekutieli (2001) method
     *
     * @param pValues a set of p-values
     * @return an array of corrected p-values
     */
    public static double @NotNull [] benjaminiYekutieli(double @NotNull ... pValues) {
        if (pValues.length == 1) return new double[]{pValues[0]};
        var n = pValues.length;
        var q = IntStream.range(0, n).mapToDouble(i -> 1 / (((double) i) + 1)).sum();
        var o = decreasingOrder(pValues);
        var ro = order(o);
        var po = sortFromIndex(pValues, o);
        var pp = IntStream.range(0, n).mapToDouble(i -> (q * n * po[i]) / (n - i)).toArray();
        return sortFromIndex(parallelMin(cumulativeMin(pp), 1), ro);
    }

    /**
     * A pass-through p-value adjustment
     *
     * @param pValues a list of p-values
     * @return the same list of p-values
     */
    @Contract(pure = true)
    public static double @NotNull [] none(double @NotNull ... pValues) {
        return pValues.clone();
    }

}
