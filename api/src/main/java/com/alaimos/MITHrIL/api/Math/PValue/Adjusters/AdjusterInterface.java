package com.alaimos.MITHrIL.api.Math.PValue.Adjusters;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Commons.Utils;

/**
 * A function which adjusts p-values on multiple hypotheses
 */
@FunctionalInterface
public interface AdjusterInterface extends ExtensionInterface {

    /**
     * Returns the name of the adjuster
     *
     * @return the name of the adjuster
     */
    default String name() {
        return Utils.camelCaseToDotCase(getClass().getSimpleName());
    }

    /**
     * Adjusts p-values
     *
     * @param pValues p-values to adjust
     * @return adjusted p-values
     */
    double[] adjust(double... pValues);

}
