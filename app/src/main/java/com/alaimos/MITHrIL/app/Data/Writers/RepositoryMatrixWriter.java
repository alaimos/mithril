package com.alaimos.MITHrIL.app.Data.Writers;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.BinaryWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class RepositoryMatrixWriter implements DataWriterInterface<RepositoryMatrix> {

    private final BinaryWriter<RepositoryMatrix> writer = new BinaryWriter<>();

    public RepositoryMatrixWriter(@NotNull Repository r) {
        writer.file(IOUtils.sanitizeFilename("repository-matrix-" + r.hashCode() + ".bin"));
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
    public DataWriterInterface<RepositoryMatrix> file(File f) {
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
    public DataWriterInterface<RepositoryMatrix> write(RepositoryMatrix data) throws IOException {
        writer.write(data);
        return this;
    }
}
