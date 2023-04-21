package com.alaimos.MITHrIL.app.Data.Writers.PHENSIM;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM.SimulationOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class ExtendedSIFWriter extends AbstractDataWriter<SimulationOutput> {

    protected Repository r;
    protected RepositoryMatrix rm;
    private final Map<String, Double> absoluteWeights = new HashMap<>();
    private String pathway = null;
    private Graph graph = null;

    public ExtendedSIFWriter(Repository r, RepositoryMatrix rm) {
        this.r  = r;
        this.rm = rm;
    }

    /**
     * Clean-up the node name.
     *
     * @param node A node
     * @return A cleaned-up name
     */
    public String cleanNodeName(@NotNull Node node) {
        var name = node.name();
        var parts = name.split(",\\s*");
        return (parts.length <= 1) ? name : parts[0];
    }

    /**
     * Compute the total weight of a given node. The total weight is obtained by summing up the absolute weight values
     * for all outgoing edges.
     *
     * @param u The node for which the total weight is computed
     * @return the total weight
     */
    private double absoluteTotalWeight(String u) {
        try {
            if (absoluteWeights.containsKey(u)) return absoluteWeights.get(u);
            var source = graph.node(u);
            double weight = 0.0;
            for (Edge e : graph.outgoingEdges(source)) {
                weight += Math.abs(e.weight());
            }
            absoluteWeights.put(u, weight);
            return weight;
        } catch (NullPointerException ignore) {
        }
        return Double.NaN;
    }

    /**
     * Write all edges for a pathway
     */
    public void writeEdges(PrintStream ps, @NotNull SimulationOutput data) {
        var indicesMap = rm.pathwayMatrix().id2Index();
        var avgPerturbations = data.nodePerturbationsAverage();
        var sdPerturbations = data.nodePerturbationsStdDev();
        var activityScores = data.nodeActivityScores();
        var pValues = data.nodePValues();
        var fdrValues = data.nodePValuesAdjusted();
        for (var edges : graph.edges().values()) {
            for (var edge : edges.values()) {
                var start = edge.source();
                var end = edge.target();
                var weight = edge.weight() / absoluteTotalWeight(start.id());
                if (Double.isFinite(weight) && weight != 0) {
                    var startId = start.id();
                    var endId = end.id();
                    var startIdx = indicesMap.getInt(startId);
                    var endIdx = indicesMap.getInt(endId);
                    writeArray(ps, new String[]{
                            startId,
                            endId,
                            Double.toString(weight),
                            pathway,
                            cleanNodeName(start),
                            Double.toString(activityScores[startIdx]),
                            Double.toString(avgPerturbations[startIdx]),
                            Double.toString(sdPerturbations[startIdx]),
                            Double.toString(pValues[startIdx]),
                            Double.toString(fdrValues[startIdx]),
                            cleanNodeName(end),
                            Double.toString(activityScores[endIdx]),
                            Double.toString(avgPerturbations[endIdx]),
                            Double.toString(sdPerturbations[endIdx]),
                            Double.toString(pValues[endIdx]),
                            Double.toString(fdrValues[endIdx])
                    });
                    ps.println();
                }
            }
        }
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public ExtendedSIFWriter write(@NotNull SimulationOutput data) throws IOException {
        pathway = r.get().id();
        graph   = r.get().graph();
        try (PrintStream ps = new PrintStream(outputStream())) {
            writeArray(ps, new String[]{
                    "# source",
                    "target",
                    "edge_weight",
                    "edge_source",
                    "source_name",
                    "source_activity_score",
                    "source_perturbation_mean",
                    "source_perturbation_stddev",
                    "source_pvalue",
                    "source_fdr",
                    "target_name",
                    "target_activity_score",
                    "target_perturbation_mean",
                    "target_perturbation_stddev",
                    "target_pvalue",
                    "target_fdr"
            });
            ps.println();
            writeEdges(ps, data);
        }
        return this;
    }
}
