package com.alaimos.MITHrIL.api.Math.StreamMedian;

import org.apache.commons.math3.stat.descriptive.rank.Median;

public class ExactMedianComputation implements StreamMedianComputationInterface {

    private static final Median MEDIAN = new Median();
    private double[] values = null;
    private int lastAvailableIndex = 0;
    private int size = 0;

    /**
     * Returns the name of this median computation method
     *
     * @return the name
     */
    @Override
    public String name() {
        return "exact";
    }

    /**
     * Returns the description of this median computation method
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Compute the exact value of the median by accumulating all the values in memory";
    }

    /**
     * If the algorithm needs the size of the stream, this method is used to set it. This method might be called
     * multiple times, so the implementation should be able to handle it.
     *
     * @param size the size of the stream
     */
    @Override
    public void numberOfElements(int size) {
        this.size = size;
    }

    /**
     * Add a new element coming from the stream.
     * If we have already reached the maximum number of elements, the oldest element is overwritten.
     *
     * @param value the value of the element
     */
    @Override
    public void addElement(double value) {
        init();
        values[lastAvailableIndex] = value;
        lastAvailableIndex = (lastAvailableIndex + 1) % values.length;
    }

    /**
     * Add multiple elements coming from the stream
     *
     * @param values the values of the elements
     */
    @Override
    public void addElements(double[] values) {
        if (values.length > this.values.length) {
            throw new IllegalArgumentException("The number of elements is greater than the maximum allowed");
        }
        if (values.length == this.values.length) {
            this.values = values;
            lastAvailableIndex = 0;
        } else {
            for (double value : values) {
                addElement(value);
            }
        }
    }

    /**
     * Get the current median value
     *
     * @return the current median value
     */
    @Override
    public double currentValue() {
        return MEDIAN.evaluate(values);
    }

    /**
     * Clears the current state of the algorithm. Notice that this method does not reset the size of the stream.
     */
    @Override
    public void clear() {
        values = null;
        lastAvailableIndex = 0;
    }

    private void init() {
        if (values == null) {
            values = new double[size];
        }
    }
}
