package com.alaimos.MITHrIL.api.Data.Pathways.Enrichment;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;

import java.util.List;
import java.util.Set;

public interface EnrichmentProbabilityComputationInterface extends ExtensionInterface {

    double computeProbability(
            String[] allExperimentNodes,
            List<String> allNodesInPathway,
            Set<String> allDiffExpNodes,
            List<String> allDiffExpNodesInPathway
    );
}
