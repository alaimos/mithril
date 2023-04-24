package com.alaimos.MITHrIL.app.Data.Writers.PHENSIMq;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIMq.SimulationOutput;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class SimulationOutputWriter extends AbstractDataWriter<SimulationOutput> {

    private final Repository r;
    private final RepositoryMatrix rm;
    private final ExpressionConstraint[] inputConstraints;
    private final Map<String, Collection<String>> directTargets;

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
            var pathwayPExpected = data.pathwayPExpected();
            var pathwayPValues = data.pathwayPValues();
            var pathwayAdjustedPValues = data.pathwayPValuesAdjusted();
            var pathwayPerturbationsAvg = data.pathwayPerturbationsAverage();
            var pathwayPerturbationsSD = data.pathwayPerturbationsStdDev();
            var nodeActivityScores = data.nodeActivityScores();
            var pathwayDistances = data.pathwayDistances();
            var nodePExpected = data.nodePExpected();
            var nodePValues = data.nodePValues();
            var nodeAdjustedPValues = data.nodePValuesAdjusted();
            var nodePerturbationsAvg = data.nodePerturbationsAverage();
            var nodePerturbationsSD = data.nodePerturbationsStdDev();
            var nodeDistances = data.nodeDistances();
            ps.println(concatArray(new String[]{
                    "# Pathway Id",
                    "Pathway Name",
                    "Node Id",
                    "Node Name",
                    "Is Endpoint",
                    "Is Direct Target",
                    "Node Activity Score",
                    "Expected Node Perturbation",
                    "Node Perturbation Std. Dev.",
                    "Node P-Value",
                    "Node Adjusted P-Value",
                    "Pathway Activity Score",
                    "Expected Pathway Perturbation",
                    "Pathway Perturbation Std. Dev.",
                    "Pathway p-value",
                    "Pathway Adjusted p-value",
                    "Direct Targets",
                    "Node Distance",
                    "Pathway Distance",
                    "Expected Node Difference",
                    "Expected Pathway Difference",
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
                    var nDist = nodeDistances[nodeIdx] == Integer.MAX_VALUE ? -1 : nodeDistances[nodeIdx];
                    var pDist = pathwayDistances[pathwayIdx] == Integer.MAX_VALUE ? -1 : pathwayDistances[pathwayIdx];
                    writeArray(ps, new String[]{
                            pathwayId,
                            pathwayName,
                            nodeId,
                            n.name(),
                            isEndpoint ? "Yes" : "No",
                            isDirectTarget ? "Yes" : "No",
                            Double.toString(nodeActivityScores[nodeIdx]),
                            Double.toString(nodePerturbationsAvg[nodeIdx]),
                            Double.toString(nodePerturbationsSD[nodeIdx]),
                            Double.toString(nodePValues[nodeIdx]),
                            Double.toString(nodeAdjustedPValues[nodeIdx]),
                            Double.toString(pathwayActivityScores[pathwayIdx]),
                            Double.toString(pathwayPerturbationsAvg[pathwayIdx]),
                            Double.toString(pathwayPerturbationsSD[pathwayIdx]),
                            Double.toString(pathwayPValues[pathwayIdx]),
                            Double.toString(pathwayAdjustedPValues[pathwayIdx]),
                            concatCollection(directTargets.get(nodeId), ","),
                            Integer.toString(nDist),
                            Integer.toString(pDist),
                            Double.toString(nodePExpected[nodeIdx]),
                            Double.toString(pathwayPExpected[pathwayIdx]),
                            }, "\t");
                    ps.println();
                }
            }
        }
        return this;
    }
}
