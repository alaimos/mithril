package com.alaimos.MITHrIL.api.Math.StreamMedian;

import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;

public class PSquaredMedianComputation implements StreamMedianComputationInterface {

    private final PSquarePercentile median = new PSquarePercentile(50);

    /**
     * Returns the name of this median computation method
     *
     * @return the name
     */
    @Override
    public String name() {
        return "p.squared";
    }

    /**
     * Returns the description of this median computation method
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Compute an approximation of the median by using the PSquared algorithm. This algorithm does not require to store all the values in memory.";
    }

    /**
     * This method is not used by this algorithm.
     *
     * @param size the size of the stream
     */
    @Override
    public void numberOfElements(int size) {
        // do nothing
    }

    /**
     * Add a new element coming from the stream. If we have already reached the maximum number of elements, the oldest
     * element is overwritten.
     *
     * @param value the value of the element
     */
    @Override
    public void addElement(double value) {
        median.increment(value);
    }

    /**
     * Add multiple elements coming from the stream
     *
     * @param values the values of the elements
     */
    @Override
    public void addElements(double[] values) {
        median.incrementAll(values);
    }

    /**
     * Add multiple elements coming from the stream
     *
     * @param values the values of the elements
     * @param start  the index of the first element to add
     */
    @Override
    public void addElements(double[] values, int start) {
        median.incrementAll(values, start, values.length - start);
    }

    /**
     * Get the current median value
     *
     * @return the current median value
     */
    @Override
    public double currentValue() {
        return median.getResult();
    }

    /**
     * Clears the current state of the algorithm. Notice that this method does not reset the size of the stream.
     */
    @Override
    public void clear() {
        median.clear();
    }
}
