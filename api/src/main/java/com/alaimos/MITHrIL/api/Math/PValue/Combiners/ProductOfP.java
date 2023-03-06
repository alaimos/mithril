package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import java.util.Arrays;

public class ProductOfP implements CombinerInterface {

    @Override
    public String getName() {
        return "product.of.p";
    }

    /**
     * Combine p-values using the product of p method
     *
     * @param pValues some p-values
     * @return a combined p-value
     */
    @Override
    public double combine(double... pValues) {
        return Arrays.stream(pValues).reduce(1, (a, b) -> a * b);
    }
}
