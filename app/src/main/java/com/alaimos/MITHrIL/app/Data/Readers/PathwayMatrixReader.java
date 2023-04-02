package com.alaimos.MITHrIL.app.Data.Readers;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Reader.BinaryReader;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Records.PathwayMatrix;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PathwayMatrixReader implements DataReaderInterface<PathwayMatrix> {

    private final BinaryReader<PathwayMatrix> reader = new BinaryReader<>(PathwayMatrix.class);
    private final MatrixFactoryInterface<?> matrixFactory;
    private final String pathwayId;

    @SuppressWarnings("unchecked")
    public PathwayMatrixReader(@NotNull Pathway p, @NotNull MatrixFactoryInterface<?> matrixFactory) {
        reader.file(IOUtils.sanitizeFilename("pathway-matrix-" + p.id() + "-" + p.hashCode() + ".bin"));
        this.pathwayId     = p.id();
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
    public DataReaderInterface<PathwayMatrix> file(String f) {
        return this;
    }

    /**
     * Set the file where data are stored
     *
     * @param f the filename
     * @return this object
     */
    @Override
    public DataReaderInterface<PathwayMatrix> file(File f) {
        return this;
    }

    /**
     * Read data
     *
     * @return the result
     * @throws IOException if something goes wrong
     */
    @Override
    public PathwayMatrix read() throws IOException {
        var pm = PathwayMatrix.of(reader.read(), matrixFactory);
        if (!pm.pathwayId().equals(pathwayId)) throw new IOException("Pathway ID mismatch");
        return pm;
    }
}
