package com.alaimos.MITHrIL.app.Data.Writer;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import com.alaimos.MITHrIL.app.Data.Records.MITHrILOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.stream.IntStream;

public class MITHrILPathwayOutputWriter extends AbstractDataWriter<MITHrILOutput> {

    private final Repository repository;
    private final RepositoryMatrix matrix;

    public MITHrILPathwayOutputWriter(Repository r, RepositoryMatrix m) {
        this.repository = r;
        this.matrix     = m;
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public DataWriterInterface<MITHrILOutput> write(@NotNull MITHrILOutput data) throws IOException {
        try (var ps = new PrintStream(outputStream())) {
            var adjustedPValues = data.pathwayAdjustedPValues();
            var pValues = data.pathwayPValues();
            var correctedAcc = data.pathwayCorrectedAccumulators();
            var rawAccumulators = data.pathwayAccumulators();
            var probabilities = data.pathwayProbabilities();
            var probabilitiesN = data.pathwayNetworkProbabilities();
            var impactFactors = data.pathwayImpactFactors();
            var sortedIndices = IntStream.range(0, correctedAcc.length)
                                         .boxed()
                                         .sorted(
                                                 Comparator.comparingDouble((Integer i) -> adjustedPValues[i])
                                                           .thenComparingDouble(i -> pValues[i])
                                                           .thenComparingDouble(i -> correctedAcc[i])
                                         )
                                         .mapToInt(Integer::intValue)
                                         .toArray();
            ps.println("# Pathway Id\tPathway Name\tRaw Accumulator\tImpact Factor\tProbability Pi\t" +
                               "Probability Network\tCorrected Accumulator\tpValue\tAdjusted pValue");
            var pathwayIndex2Id = matrix.index2Id();
            for (var i : sortedIndices) {
                var pathway = repository.virtualPathway(pathwayIndex2Id.get(i));
                writeArray(ps, new String[]{
                        pathway.id(),
                        pathway.name(),
                        Double.toString(rawAccumulators[i]),
                        Double.toString(impactFactors[i]),
                        Double.toString(probabilities[i]),
                        Double.toString(probabilitiesN[i]),
                        Double.toString(correctedAcc[i]),
                        Double.toString(pValues[i]),
                        Double.toString(adjustedPValues[i])
                });
                ps.println();
            }
        }
        return this;
    }
}
