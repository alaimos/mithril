package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Enrichment.EnrichmentProbabilityComputationInterface;

public class EnrichmentProbabilityComputationInterfaceType implements ExtensionTypeInterface {
    @Override
    public String name() {
        return "enrichment-probability";
    }

    @Override
    public String description() {
        return "The algorithms used to compute the enrichment probability of a pathway (whether the differentially expressed genes in the pathway are significantly higher than expected).";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return EnrichmentProbabilityComputationInterface.class;
    }
}
