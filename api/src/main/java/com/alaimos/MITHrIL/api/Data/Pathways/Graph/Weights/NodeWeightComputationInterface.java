package com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;

/**
 * Compute the weight of a node
 */
@FunctionalInterface
public interface NodeWeightComputationInterface extends ExtensionInterface {

    default String name() {
        return "unknown";
    }

    double weight(Node n);

}
