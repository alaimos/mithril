package com.alaimos.MITHrIL.api.Math.PValue.Interfaces;

/**
 * A function which adjusts p-values on multiple hypotheses
 *
 * @author Salvatore Alaimo, Ph.D.
 * @version 2.0.0.0
 * @since 03/01/2016
 */
@FunctionalInterface
public interface Adjuster {

    /**
     * Adjusts p-values
     *
     * @param pValues p-values to adjust
     * @return adjusted p-values
     */
    double[] adjust(double... pValues);

}
