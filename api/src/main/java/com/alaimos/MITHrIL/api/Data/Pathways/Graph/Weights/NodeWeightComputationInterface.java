package com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import org.pf4j.ExtensionPoint;

/**
 * Compute the weight of a node
 */
@FunctionalInterface
public interface NodeWeightComputationInterface extends ExtensionPoint {

    default String name() {
        return "unknown";
    }

    double weight(Node n);

}
