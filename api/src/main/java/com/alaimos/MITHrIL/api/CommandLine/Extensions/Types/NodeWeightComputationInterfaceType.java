package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.NodeWeightComputationInterface;

public class NodeWeightComputationInterfaceType implements ExtensionTypeInterface {
    @Override
    public String name() {
        return "node-weight-computation";
    }

    @Override
    public String description() {
        return "The algorithms used to compute node weights in the pathway graph";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return NodeWeightComputationInterface.class;
    }
}
