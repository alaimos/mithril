package com.alaimos.MITHrIL.api.Math;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Some vector operations (vector in the mathematical sense not the Java one)
 */
public class Vectors {

    /**
     * Returns the parallel minimum of one array and a value
     *
     * @param array the array
     * @param value the value
     * @return an array with the minimum of each array element and the value
     */
    @Contract(pure = true)
    public static double @NotNull [] parallelMin(double @NotNull [] array, double value) {
        var tmp = new double[array.length];
        for (var i = 0; i < tmp.length; i++) {
            tmp[i] = Math.min(array[i], value);
        }
        return tmp;
    }

    /**
     * Returns the parallel minimum of two arrays
     *
     * @param array1 the first array
     * @param array2 the second array
     * @return an array with the minimum of each array element and the corresponding element in the second array
     */
    @Contract(pure = true)
    public static double @NotNull [] parallelMin(double @NotNull [] array1, double @NotNull [] array2) {
        var tmp = new double[array1.length];
        for (var i = 0; i < tmp.length; i++) {
            tmp[i] = Math.min(array1[i], array2[i]);
        }
        return tmp;
    }

    /**
     * Returns the parallel maximum of one array and a value
     *
     * @param array the array
     * @param value the value
     * @return an array with the maximum of each array element and the value
     */
    @Contract(pure = true)
    public static double @NotNull [] parallelMax(double @NotNull [] array, double value) {
        var tmp = new double[array.length];
        for (var i = 0; i < tmp.length; i++) {
            tmp[i] = Math.max(array[i], value);
        }
        return tmp;
    }

    /**
     * Returns the parallel maximum of two arrays
     *
     * @param array1 the first array
     * @param array2 the second array
     * @return an array with the maximum of each array element and the corresponding element in the second array
     */
    @Contract(pure = true)
    public static double @NotNull [] parallelMax(double @NotNull [] array1, double @NotNull [] array2) {
        var tmp = new double[array1.length];
        for (var i = 0; i < tmp.length; i++) {
            tmp[i] = Math.max(array1[i], array2[i]);
        }
        return tmp;
    }

    /**
     * Returns the cumulative minimum of an array
     *
     * @param array the array
     * @return an array with the cumulative minimum of the array
     */
    @Contract(pure = true)
    public static double @NotNull [] cumulativeMin(double @NotNull [] array) {
        var tmp = new double[array.length];
        if (tmp.length == 0) return tmp;
        tmp[0] = array[0];
        for (var i = 1; i < tmp.length; i++) {
            tmp[i] = Math.min(tmp[i - 1], array[i]);
        }
        return tmp;
    }

    /**
     * Returns the cumulative maximum of an array
     *
     * @param array the array
     * @return an array with the cumulative maximum of the array
     */
    @Contract(pure = true)
    public static double @NotNull [] cumulativeMax(double @NotNull [] array) {
        var tmp = new double[array.length];
        if (tmp.length == 0) return tmp;
        tmp[0] = array[0];
        for (var i = 1; i < tmp.length; i++) {
            tmp[i] = Math.max(tmp[i - 1], array[i]);
        }
        return tmp;
    }

    /**
     * Returns the sequence of indices that would sort an array
     *
     * @param array the array
     * @return the sequence of indices that would sort the array
     */
    public static int[] order(int @NotNull [] array) {
        return IntStream.range(0, array.length).boxed().sorted(Comparator.comparingInt(o -> array[o])).mapToInt(v -> v).toArray();
    }

    /**
     * Returns the sequence of indices that would sort an array
     *
     * @param array the array
     * @return the sequence of indices that would sort the array
     */
    public static int[] order(double @NotNull [] array) {
        return IntStream.range(0, array.length).boxed().sorted(Comparator.comparingDouble(o -> array[o])).mapToInt(v -> v).toArray();
    }

    /**
     * Returns the sequence of indices that would sort an array
     *
     * @param array the array
     * @param <T>   the type of the array
     * @return the sequence of indices that would sort the array
     */
    public static <T extends Comparable<T>> int[] order(T @NotNull [] array) {
        return IntStream.range(0, array.length).boxed().sorted(Comparator.comparing(o -> array[o])).mapToInt(v -> v).toArray();
    }

    /**
     * Returns the sequence of indices that would sort an array in decreasing order
     *
     * @param array the array
     * @return the sequence of indices that would sort the array in decreasing order
     */
    public static int[] decreasingOrder(int @NotNull [] array) {
        return IntStream.range(0, array.length).boxed().sorted((o1, o2) -> Integer.compare(array[o2], array[o1])).mapToInt(v -> v).toArray();
    }

    /**
     * Returns the sequence of indices that would sort an array in decreasing order
     *
     * @param array the array
     * @return the sequence of indices that would sort the array in decreasing order
     */
    public static int[] decreasingOrder(double @NotNull [] array) {
        return IntStream.range(0, array.length).boxed().sorted((o1, o2) -> Double.compare(array[o2], array[o1])).mapToInt(v -> v).toArray();
    }

    /**
     * Returns the sequence of indices that would sort an array in decreasing order
     *
     * @param array the array
     * @param <T>   the type of the array
     * @return the sequence of indices that would sort the array in decreasing order
     */
    public static <T extends Comparable<T>> int[] decreasingOrder(T @NotNull [] array) {
        return IntStream.range(0, array.length).boxed().sorted((o1, o2) -> array[o2].compareTo(array[o1])).mapToInt(v -> v).toArray();
    }

    /**
     * Sorts an array from a sequence of indices
     *
     * @param array the array
     * @param index the index
     * @return the sorted array
     */
    @Contract(pure = true)
    public static int @NotNull [] sortFromIndex(int @NotNull [] array, int @NotNull [] index) {
        if (array.length != index.length) throw new RuntimeException("Arrays should have the same length");
        var r = new int[array.length];
        for (var i = 0; i < index.length; i++) {
            r[i] = array[index[i]];
        }
        return r;
    }

    /**
     * Sorts an array from a sequence of indices
     *
     * @param array the array
     * @param index the index
     * @return the sorted array
     */
    @Contract(pure = true)
    public static double @NotNull [] sortFromIndex(double @NotNull [] array, int @NotNull [] index) {
        if (array.length != index.length) throw new RuntimeException("Arrays should have the same length");
        var r = new double[array.length];
        for (var i = 0; i < index.length; i++) {
            r[i] = array[index[i]];
        }
        return r;
    }

    /**
     * Sorts an array from a sequence of indices
     *
     * @param array the array
     * @param index the index
     * @param <T>   the type of the array
     * @return the sorted array
     */
    public static <T> T[] sortFromIndex(T @NotNull [] array, int @NotNull [] index) {
        if (array.length != index.length) throw new RuntimeException("Arrays should have the same length");
        @SuppressWarnings("unchecked") var r = (T[]) Array.newInstance(array.getClass(), array.length);
        for (var i = 0; i < index.length; i++) {
            r[i] = array[index[i]];
        }
        return r;
    }


    /**
     * Computes the absolute value of each element in the array in place
     *
     * @param a the array
     * @return the array with the absolute value of each element
     */
    @Contract("_ -> param1")
    public static double[] absInPlace(double @NotNull [] a) {
        for (var i = 0; i < a.length; i++) {
            a[i] = Math.abs(a[i]);
        }
        return a;
    }


    /**
     * Computes the signum of each element in the array in place
     *
     * @param a the array
     * @return the array with the signum of each element
     */
    @Contract("_ -> param1")
    public static double[] signumInPlace(double @NotNull [] a) {
        for (var i = 0; i < a.length; i++) {
            a[i] = Math.signum(a[i]);
        }
        return a;
    }

    /**
     * Computes the sum of two arrays in place (a1 = a1 + a2)
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return the first array with the sum of the two arrays
     */
    @Contract("_, _ -> param1")
    public static double[] sumInPlace(double @NotNull [] a1, double @NotNull [] a2) {
        assert a1.length == a2.length;
        for (var i = 0; i < a1.length; i++) {
            a1[i] = a1[i] + a2[i];
        }
        return a1;
    }

    /**
     * Computes the difference of two arrays in place (a1 = a1 - a2)
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return the first array with the difference of the two arrays
     */
    @Contract("_, _ -> param1")
    public static double[] diffInPlace(double @NotNull [] a1, double @NotNull [] a2) {
        assert a1.length == a2.length;
        for (var i = 0; i < a1.length; i++) {
            a1[i] = a1[i] - a2[i];
        }
        return a1;
    }

    /**
     * Computes the product of two arrays in place (a1 = a1 * a2)
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return the first array with the product of the two arrays
     */
    @Contract("_, _ -> param1")
    public static double[] mulInPlace(double @NotNull [] a1, double @NotNull [] a2) {
        assert a1.length == a2.length;
        for (var i = 0; i < a1.length; i++) {
            a1[i] = a1[i] * a2[i];
        }
        return a1;
    }

    /**
     * Computes the absolute value of each element in the array and returns a new array
     *
     * @param a the array
     * @return a new array with the absolute value of each element
     */
    @Contract(pure = true)
    public static double @NotNull [] abs(double @NotNull [] a) {
        var result = new double[a.length];
        for (var i = 0; i < a.length; i++) {
            result[i] = Math.abs(a[i]);
        }
        return result;
    }

    /**
     * Computes the signum of each element in the array and returns a new array
     *
     * @param a the array
     * @return a new array with the signum of each element
     */
    @Contract(pure = true)
    public static double @NotNull [] signum(double @NotNull [] a) {
        var result = new double[a.length];
        for (var i = 0; i < a.length; i++) {
            result[i] = Math.signum(a[i]);
        }
        return result;
    }

    /**
     * Computes the sum of two arrays and returns a new array
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return a new array with the sum of the two arrays
     */
    @Contract(pure = true)
    public static double @NotNull [] sum(double @NotNull [] a1, double @NotNull [] a2) {
        assert a1.length == a2.length;
        var result = new double[a1.length];
        for (var i = 0; i < a1.length; i++) {
            result[i] = a1[i] + a2[i];
        }
        return result;
    }

    /**
     * Computes the difference of two arrays and returns a new array
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return a new array with the difference of the two arrays
     */
    @Contract(pure = true)
    public static double @NotNull [] diff(double @NotNull [] a1, double @NotNull [] a2) {
        assert a1.length == a2.length;
        var result = new double[a1.length];
        for (var i = 0; i < a1.length; i++) {
            result[i] = a1[i] - a2[i];
        }
        return result;
    }

    /**
     * Computes the product of two arrays and returns a new array
     *
     * @param a1 the first array
     * @param a2 the second array
     * @return a new array with the product of the two arrays
     */
    @Contract(pure = true)
    public static double @NotNull [] mul(double @NotNull [] a1, double @NotNull [] a2) {
        assert a1.length == a2.length;
        var result = new double[a1.length];
        for (var i = 0; i < a1.length; i++) {
            result[i] = a1[i] * a2[i];
        }
        return result;
    }

}
