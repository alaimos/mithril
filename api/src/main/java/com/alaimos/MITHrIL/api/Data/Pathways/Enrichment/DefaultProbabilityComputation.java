package com.alaimos.MITHrIL.api.Data.Pathways.Enrichment;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class DefaultProbabilityComputation implements EnrichmentProbabilityComputationInterface {

    public String name() {
        return "default";
    }

    public String description() {
        return "Default probability computation (it uses the hypergeometric distribution to test the enrichment)";
    }

    @Override
    public double computeProbability(
            @NotNull String @NotNull [] allExperimentNodes,
            @NotNull List<String> allNodesInPathway,
            @NotNull Set<String> allDiffExpNodes,
            @NotNull List<String> allDiffExpNodesInPathway
    ) {
        var p = allExperimentNodes.length;
        var m = allNodesInPathway.size();
        var k = allDiffExpNodes.size();
        var x = allDiffExpNodesInPathway.size();
        return new HypergeometricDistribution(p, m, k).upperCumulativeProbability(x);
    }
}
