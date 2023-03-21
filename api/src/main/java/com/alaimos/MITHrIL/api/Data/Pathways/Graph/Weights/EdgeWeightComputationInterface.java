package com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import org.pf4j.ExtensionPoint;

/**
 * Compute the weight of an edge
 */
@FunctionalInterface
public interface EdgeWeightComputationInterface extends ExtensionInterface {

    default String name() {
        return "unknown";
    }

    double weight(Edge e);

}
