package com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import org.jetbrains.annotations.NotNull;

public class DefaultNodeWeightComputationMethod implements NodeWeightComputationInterface {
    @Override
    public String name() {
        return "default";
    }

    @Override
    public double weight(@NotNull Node n) {
        return n.type().sign();
    }
}
