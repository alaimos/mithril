package com.alaimos.MITHrIL.app.Data.Records;

public record MITHrILOutput(double[] nodePerturbations, double[] nodeAccumulators, double[] pathwayAccumulators,
                            double[] pathwayCorrectedAccumulators, double[] pathwayImpactFactors, double[] nodePValues,
                            double[] nodeAdjustedPValues, double[] pathwayPValues, double[] pathwayAdjustedPValues) {
}
