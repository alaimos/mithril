package com.alaimos.MITHrIL.app.Data.Writers;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Writer.BinaryWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import com.alaimos.MITHrIL.app.Data.Records.PathwayMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PathwayMatrixWriter implements DataWriterInterface<PathwayMatrix> {

    private final BinaryWriter<PathwayMatrix> writer = new BinaryWriter<>();

    public PathwayMatrixWriter(@NotNull Pathway p) {
        writer.file(IOUtils.sanitizeFilename("pathway-matrix-" + p.id() + "-" + p.hashCode() + ".bin"));
    }

    /**
     * Get the file where data are stored
     *
     * @return the file
     */
    @Override
    public File file() {
        return writer.file();
    }

    /**
     * Set the filename where data are stored (the file will be created in any location)
     *
     * @param f the filename
     * @return this object for a fluent interface
     */
    @Override
    public DataWriterInterface<PathwayMatrix> file(File f) {
        return this;
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    @Override
    public DataWriterInterface<PathwayMatrix> write(PathwayMatrix data) throws IOException {
        writer.write(data);
        return this;
    }
}
