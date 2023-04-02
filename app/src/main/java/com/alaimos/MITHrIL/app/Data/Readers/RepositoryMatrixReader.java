package com.alaimos.MITHrIL.app.Data.Readers;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Reader.BinaryReader;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class RepositoryMatrixReader implements DataReaderInterface<RepositoryMatrix> {

    private final BinaryReader<RepositoryMatrix> reader = new BinaryReader<>(RepositoryMatrix.class);
    private final MatrixFactoryInterface<?> matrixFactory;

    public RepositoryMatrixReader(@NotNull Repository r, @NotNull MatrixFactoryInterface<?> matrixFactory) {
        reader.file(IOUtils.sanitizeFilename("repository-matrix-" + r.hashCode() + ".bin"));
        this.matrixFactory = matrixFactory;
    }

    /**
     * Get the filename where data are stored
     *
     * @return the filename
     */
    @Override
    public String file() {
        return reader.file();
    }

    /**
     * Set the filename where data are stored
     *
     * @param f the filename
     * @return this object
     */
    @Override
    public DataReaderInterface<RepositoryMatrix> file(String f) {
        return this;
    }

    /**
     * Set the file where data are stored
     *
     * @param f the filename
     * @return this object
     */
    @Override
    public DataReaderInterface<RepositoryMatrix> file(File f) {
        return this;
    }

    /**
     * Read data
     *
     * @return the result
     * @throws IOException if something goes wrong
     */
    @Override
    public RepositoryMatrix read() throws IOException {
        return RepositoryMatrix.of(reader.read(), matrixFactory);
    }
}
