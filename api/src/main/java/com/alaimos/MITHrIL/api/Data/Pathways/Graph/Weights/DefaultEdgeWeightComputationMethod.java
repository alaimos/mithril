package com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import org.jetbrains.annotations.NotNull;

public class DefaultEdgeWeightComputationMethod implements EdgeWeightComputationInterface {
    @Override
    public String name() {
        return "default";
    }

    public String description() {
        return "Default edge weight computation method";
    }

    @Override
    public double weight(@NotNull Edge e) {
        if (!e.isMultiEdge()) {
            return e.details().get(0).subtype().weight();
        }

        double weight = 0.0;
        double absWeight = 0.0;
        for (var d : e.details()) {
            weight += d.subtype().weight();
            absWeight += Math.abs(d.subtype().weight());
        }
        if (weight == 0.0) return 0.0;
        return weight / absWeight; // Normalizes in the range [-1,1]
    }
}
