package com.alaimos.MITHrIL.app.Data.Writers.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;

public class NodesIndexWriter extends AbstractDataWriter<Pathway> {

    /**
     * Write the list of nodes in a pathway to a tsv file with the following columns: id, Name, Type, Aliases
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    @Override
    public DataWriterInterface<Pathway> write(@NotNull Pathway data) throws IOException {
        try (var ps = new PrintStream(outputStream())) {
            ps.println("#Id\tName\tType\tAliases");
            for (var n : data.graph().nodes().values()) {
                writeDelimited(
                        ps, "\t",
                        n.id(), n.name(), n.type().name(), concatCollection(n.aliases(), ",")
                );
                ps.println();
            }
        }
        return this;
    }
}
