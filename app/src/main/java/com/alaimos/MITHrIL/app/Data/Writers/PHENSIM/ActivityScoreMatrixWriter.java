package com.alaimos.MITHrIL.app.Data.Writers.PHENSIM;

import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM.SimulationOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;

public class ActivityScoreMatrixWriter extends AbstractDataWriter<SimulationOutput> {

    private final RepositoryMatrix repositoryMatrix;
    private final boolean printPathways;

    public ActivityScoreMatrixWriter(RepositoryMatrix repositoryMatrix, boolean printPathways) {
        this.repositoryMatrix = repositoryMatrix;
        this.printPathways    = printPathways;
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public ActivityScoreMatrixWriter write(@NotNull SimulationOutput data) throws IOException {
        var index2Id = (printPathways) ? repositoryMatrix.index2Id() : repositoryMatrix.pathwayMatrix().index2Id();
        try (PrintStream ps = new PrintStream(outputStream())) {
            var sb = new StringBuilder();
            for (var i = 0; i < index2Id.size(); i++) {
                sb.append(index2Id.get(i));
                if (i < index2Id.size() - 1) sb.append("\t");
            }
            ps.println(sb);
            var runs = data.runs();
            for (var j = 0; j < runs.length; j++) {
                sb = new StringBuilder();
                var row = printPathways ? runs[j].pathwayActivityScores() : runs[j].nodeActivityScores();
                for (var i = 0; i < index2Id.size(); i++) {
                    sb.append(row[i]);
                    if (j < runs.length - 1) sb.append("\t");
                }
                ps.println(sb);
            }
        }
        return this;
    }
}
