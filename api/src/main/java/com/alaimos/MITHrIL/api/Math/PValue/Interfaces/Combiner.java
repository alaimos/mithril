package com.alaimos.MITHrIL.api.Math.PValue.Interfaces;

/**
 * A function which combines input p-values
 *
 * @author Salvatore Alaimo, Ph.D.
 * @version 2.0.0.0
 * @since 03/01/2016
 */
@FunctionalInterface
public interface Combiner {

    /**
     * Combines p-values
     *
     * @param pValues p-values to combine
     * @return combined p-value
     */
    double combine(double... pValues);

}
