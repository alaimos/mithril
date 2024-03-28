package com.alaimos.MITHrIL.app.Data.Writers.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

public class PathwayToEdgesIndexWriter extends AbstractDataWriter<Collection<Repository.VirtualPathway>> {

    /**
     * For each pathway contained in the metapathway, write the list of edges belonging to it to a tsv file with the
     * following columns: Pathway Id, Pathway Name, Edge Start, Edge End
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    @Override
    public DataWriterInterface<Collection<Repository.VirtualPathway>> write(
            @NotNull Collection<Repository.VirtualPathway> data
    ) throws IOException {
        try (var ps = new PrintStream(outputStream())) {
            ps.println("#Pathway Id\tPathway Name\tEdge Start\tEdge End");
            data.forEach((pathway) -> {
                var pId = pathway.id();
                var pName = pathway.name();
                pathway.edges().forEach(e -> {
                    writeDelimited(ps, "\t", pId, pName, e.first(), e.second());
                    ps.println();
                });
            });
        }
        return this;
    }
}
