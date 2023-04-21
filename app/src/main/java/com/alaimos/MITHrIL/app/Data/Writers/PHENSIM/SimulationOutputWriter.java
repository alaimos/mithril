package com.alaimos.MITHrIL.app.Data.Writers.PHENSIM;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM.SimulationOutput;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SimulationOutputWriter extends AbstractDataWriter<SimulationOutput> {

    protected final Repository r;
    protected final RepositoryMatrix rm;
    protected final ExpressionConstraint[] inputConstraints;
    protected final Map<String, Collection<String>> directTargets;

    public SimulationOutputWriter(Repository r, RepositoryMatrix rm, ExpressionConstraint[] inputConstraints) {
        this.r                = r;
        this.rm               = rm;
        this.inputConstraints = inputConstraints;
        this.directTargets    = findDirectTargets();
    }

    @Contract(pure = true)
    private @NotNull Map<String, Collection<String>> findDirectTargets() {
        var result = new HashMap<String, Collection<String>>();
        var graph = r.get().graph();
        for (var constraint : inputConstraints) {
            var sourceId = constraint.nodeId();
            var node = graph.node(sourceId);
            if (node == null) continue;
            var outgoingEdges = graph.outgoingEdges(node);
            if (outgoingEdges == null) continue;
            for (var edge : outgoingEdges) {
                var target = edge.target().id();
                result.computeIfAbsent(target, k -> new HashSet<>()).add(sourceId);
            }

        }
        return result;
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public SimulationOutputWriter write(@NotNull SimulationOutput data) throws IOException {
        try (PrintStream ps = new PrintStream(outputStream())) {
            var graph = r.get().graph();
            var pathwayId2Idx = rm.id2Index();
            var nodeId2Idx = rm.pathwayMatrix().id2Index();
            var endpointSet = new HashSet<>(graph.endpoints());
            var pathwayActivityScores = data.pathwayActivityScores();
            var pathwayPValues = data.pathwayPValues();
            var pathwayAdjustedPValues = data.pathwayPValuesAdjusted();
            var pathwayPerturbations = data.pathwayPerturbationsAverage();
            var pathwayCounters = data.pathwayCounters();
            var nodeActivityScores = data.nodeActivityScores();
            var nodePValues = data.nodePValues();
            var nodeAdjustedPValues = data.nodePValuesAdjusted();
            var nodeCounters = data.nodeCounters();
            var nodePerturbations = data.nodePerturbationsAverage();
            ps.println(concatArray(new String[]{
                    "# Pathway Id",
                    "Pathway Name",
                    "Node Id",
                    "Node Name",
                    "Is Endpoint",
                    "Is Direct Target",
                    "Activity Score",
                    "P-Value",
                    "Adjusted P-Value",
                    "Counters (Activation, Inhibition, Others)",
                    "Pathway Activity Score",
                    "Pathway p-value",
                    "Pathway Adjusted p-value",
                    "Pathway Counters (Activation, Inhibition, Others)",
                    "Direct Targets",
                    "Average Node Perturbation",
                    "Average Pathway Perturbation"
            }, "\t"));
            for (var p : r.virtualPathways()) {
                var pathwayId = p.id();
                var pathwayIdx = pathwayId2Idx.getInt(pathwayId);
                var pathwayName = p.name();
                var pathwayNodes = p.nodes();
                for (var n : pathwayNodes) {
                    var nodeId = n.id();
                    var nodeIdx = nodeId2Idx.getInt(nodeId);
                    var isEndpoint = endpointSet.contains(nodeId);
                    var isDirectTarget = directTargets.containsKey(nodeId);
                    writeArray(ps, new String[]{
                            pathwayId,
                            pathwayName,
                            nodeId,
                            n.name(),
                            isEndpoint ? "Yes" : "No",
                            isDirectTarget ? "Yes" : "No",
                            Double.toString(nodeActivityScores[nodeIdx]),
                            Double.toString(nodePValues[nodeIdx]),
                            Double.toString(nodeAdjustedPValues[nodeIdx]),
                            concatArray(nodeCounters[nodeIdx], ","),
                            Double.toString(pathwayActivityScores[pathwayIdx]),
                            Double.toString(pathwayPValues[pathwayIdx]),
                            Double.toString(pathwayAdjustedPValues[pathwayIdx]),
                            concatArray(pathwayCounters[pathwayIdx], ","),
                            concatCollection(directTargets.get(nodeId), ","),
                            Double.toString(pathwayPerturbations[pathwayIdx]),
                            Double.toString(nodePerturbations[nodeIdx])
                    });
                }
            }
        }
        return this;
    }
}
