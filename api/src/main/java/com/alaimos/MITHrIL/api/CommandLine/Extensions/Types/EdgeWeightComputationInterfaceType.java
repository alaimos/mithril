package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.EdgeWeightComputationInterface;

public class EdgeWeightComputationInterfaceType implements ExtensionTypeInterface {

    @Override
    public String name() {
        return "edge-weight-computation";
    }

    @Override
    public String description() {
        return "The algorithms used to compute edge weights in the pathway graph";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return EdgeWeightComputationInterface.class;
    }
}
