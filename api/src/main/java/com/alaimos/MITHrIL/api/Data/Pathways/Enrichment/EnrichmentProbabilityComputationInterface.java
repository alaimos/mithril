package com.alaimos.MITHrIL.api.Data.Pathways.Enrichment;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.pf4j.ExtensionPoint;

import java.util.List;

@FunctionalInterface
public interface EnrichmentProbabilityComputationInterface extends ExtensionPoint {

    default String name() {
        return Utils.camelCaseToDotCase(getClass().getSimpleName());
    }

    double computeProbability(
            List<String> allExperimentNodes,
            List<String> allNodesInPathway,
            List<String> allDiffExpNodes,
            List<String> allDiffExpNodesInPathway
    );
}
