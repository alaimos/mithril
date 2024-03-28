package com.alaimos.MITHrIL.app.Data.Writers.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;

public class EdgesIndexWriter extends AbstractDataWriter<Pathway> {

    /**
     * Write the list of edges in a pathway to a tsv file with the following columns: Source Id, Target Id, Type,
     * Subtype, Weight
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    @Override
    public DataWriterInterface<Pathway> write(@NotNull Pathway data) throws IOException {
        try (var ps = new PrintStream(outputStream())) {
            ps.println("#Source\tTarget\tType\tSubtype\tWeight");
            for (var el : data.graph().edges().values()) {
                for (var e : el.values()) {
                    for (var ed : e.details()) {
                        writeDelimited(
                                ps, "\t",
                                e.source().id(), e.target().id(), ed.type(), ed.subtype(), e.weight()
                        );
                        ps.println();
                    }
                }
            }
        }
        return this;
    }
}
