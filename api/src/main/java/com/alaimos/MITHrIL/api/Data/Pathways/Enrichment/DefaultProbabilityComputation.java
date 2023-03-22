package com.alaimos.MITHrIL.api.Data.Pathways.Enrichment;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DefaultProbabilityComputation implements EnrichmentProbabilityComputationInterface {

    public String name() {
        return "default";
    }

    @Override
    public double computeProbability(@NotNull List<String> allExperimentNodes,
                                     @NotNull List<String> allNodesInPathway,
                                     @NotNull List<String> allDiffExpNodes,
                                     @NotNull List<String> allDiffExpNodesInPathway) {
        var p = allExperimentNodes.size();
        var m = allNodesInPathway.size();
        var k = allDiffExpNodes.size();
        var x = allDiffExpNodesInPathway.size();
        return new HypergeometricDistribution(p, m, k).upperCumulativeProbability(x);
    }
}
