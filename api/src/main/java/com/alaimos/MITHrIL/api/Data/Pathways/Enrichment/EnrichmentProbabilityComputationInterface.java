package com.alaimos.MITHrIL.api.Data.Pathways.Enrichment;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.pf4j.ExtensionPoint;

import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface EnrichmentProbabilityComputationInterface extends ExtensionPoint {

    default String name() {
        return Utils.camelCaseToDotCase(getClass().getSimpleName());
    }

    double computeProbability(
            String[] allExperimentNodes,
            List<String> allNodesInPathway,
            Set<String> allDiffExpNodes,
            List<String> allDiffExpNodesInPathway
    );
}
