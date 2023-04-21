package com.alaimos.MITHrIL.app.Data.Writers.MITHrIL;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import com.alaimos.MITHrIL.app.Data.Records.MITHrILOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;

public class MITHrILPerturbationDetailedOutputWriter extends AbstractDataWriter<MITHrILOutput> {

    protected Repository repository;
    protected RepositoryMatrix matrix;
    protected boolean onlyEndpoints;

    public MITHrILPerturbationDetailedOutputWriter(
            Repository repository, RepositoryMatrix matrix, boolean onlyEndpoints
    ) {
        this.repository    = repository;
        this.onlyEndpoints = onlyEndpoints;
        this.matrix        = matrix;
    }

    private void writeEntry(
            PrintStream ps, String pathwayId, String pathwayName, String nodeId, String nodeName,
            double perturbation, double accumulator, double p, double pAdjusted
    ) {
        writeArray(ps, new String[]{
                pathwayId,
                pathwayName,
                nodeId,
                nodeName,
                Double.toString(perturbation),
                Double.toString(accumulator),
                Double.toString(p),
                Double.toString(pAdjusted)
        });
        ps.println();
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public DataWriterInterface<MITHrILOutput> write(@NotNull MITHrILOutput data) throws IOException {
        try (PrintStream ps = new PrintStream(outputStream())) {
            var perturbations = data.nodePerturbations();
            var accumulators = data.nodeAccumulators();
            var pValues = data.nodePValues();
            var pValuesAdjusted = data.nodeAdjustedPValues();
            var nodeId2Index = matrix.pathwayMatrix().id2Index();
            var endpoints = new HashSet<>(repository.get().graph().endpoints());
            ps.println("# Pathway Id\tPathway Name\tGene Id\tGene Name\tPerturbation\tAccumulator\tpValue");
            String pathwayId, pathwayName, nodeId;
            int nodeIdx;
            Collection<Node> nodes;
            for (var p : repository.virtualPathways()) {
                pathwayId   = p.id();
                pathwayName = p.name();
                nodes       = p.nodes();
                for (var n : nodes) {
                    nodeId = n.id();
                    if (onlyEndpoints && !endpoints.contains(nodeId)) continue;
                    nodeIdx = nodeId2Index.getInt(nodeId);
                    writeEntry(
                            ps, pathwayId, pathwayName, nodeId, n.name(), perturbations[nodeIdx], accumulators[nodeIdx],
                            pValues[nodeIdx], pValuesAdjusted[nodeIdx]
                    );
                }
            }
        }
        return this;
    }

}
