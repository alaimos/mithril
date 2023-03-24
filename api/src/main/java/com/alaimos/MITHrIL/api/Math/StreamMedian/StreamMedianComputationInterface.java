package com.alaimos.MITHrIL.api.Math.StreamMedian;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;

/**
 * This interface defines the interface of the methods that could be used to compute the median of a numeric stream.
 */
public interface StreamMedianComputationInterface extends ExtensionInterface {

    /**
     * Returns the name of this median computation method
     *
     * @return the name
     */
    String name();

    /**
     * Returns the description of this median computation method
     *
     * @return the description
     */
    String description();

    /**
     * If the algorithm needs the size of the stream, this method is used to set it. This method might be called
     * multiple times, so the implementation should be able to handle it.
     *
     * @param size the size of the stream
     */
    void numberOfElements(int size);

    /**
     * Add a new element coming from the stream
     *
     * @param value the value of the element
     */
    void addElement(double value);

    /**
     * Add multiple elements coming from the stream
     *
     * @param values the values of the elements
     */
    void addElements(double[] values);

    /**
     * Get the current median value
     *
     * @return the current median value
     */
    double currentValue();

    /**
     * Clears the current state of the algorithm. Notice that this method does not reset the size of the stream.
     */
    void clear();

}
