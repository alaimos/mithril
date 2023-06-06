package com.alaimos.MITHrIL.app.Data.Writers.ReversePhensim;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM.SimulationOutput;
import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.CoverRanking;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class OutputWriter extends AbstractDataWriter<List<CoverRanking.RankedSet>> {

    private final int universeSize;

    public OutputWriter(int universeSize) {
        this.universeSize = universeSize;
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public OutputWriter write(@NotNull List<CoverRanking.RankedSet> data) throws IOException {
        try (PrintStream ps = new PrintStream(outputStream())) {
            ps.println(concatArray(new String[]{
                    "# Set",
                    "Coverage",
                    "Percentage",
                    "Covered Nodes"
            }, "\t"));
            for (var rankedSet : data) {
                writeArray(ps, new String[]{
                        expressionConstraintsToString(rankedSet.covering()),
                        Integer.toString(rankedSet.coverage()),
                        Double.toString((double) rankedSet.coverage() / universeSize),
                        expressionConstraintsToString(rankedSet.coveredNodes())
                });
                ps.println();
            }
        }
        return this;
    }

    private static @NotNull String expressionConstraintToString(@NotNull ExpressionConstraint constraint) {
        return constraint.nodeId() + "," + constraint.direction().toString();
    }

    private @NotNull String expressionConstraintsToString(@NotNull ExpressionConstraint[] constraints) {
        return concatArray(
                Arrays.stream(constraints).map(OutputWriter::expressionConstraintToString).toArray(String[]::new),
                ";"
        );
    }
}
