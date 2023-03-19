package com.alaimos.MITHrIL.app.Data.Reader;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Reader.BinaryReader;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.app.Data.Pathways.PathwayMatrixBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PathwayMatrixReader<E extends MatrixInterface<E>> implements DataReaderInterface<PathwayMatrixBuilder.BuilderResult<E>> {

    @SuppressWarnings("rawtypes")
    private BinaryReader<PathwayMatrixBuilder.BuilderResult> reader = new BinaryReader<>(PathwayMatrixBuilder.BuilderResult.class);

    @SuppressWarnings("unchecked")
    public PathwayMatrixReader(@NotNull Repository r, @NotNull Pathway p) {
        reader.file("pathway-matrix-" + r.hashCode() + "-" + p.hashCode() + ".bin");
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
    public DataReaderInterface<PathwayMatrixBuilder.BuilderResult<E>> file(String f) {
        return this;
    }

    /**
     * Set the file where data are stored
     *
     * @param f the filename
     * @return this object
     */
    @Override
    public DataReaderInterface<PathwayMatrixBuilder.BuilderResult<E>> file(File f) {
        return this;
    }

    /**
     * Read data
     *
     * @return the result
     * @throws IOException if something goes wrong
     */
    @Override
    @SuppressWarnings("unchecked")
    public PathwayMatrixBuilder.BuilderResult<E> read() throws IOException {
        return (PathwayMatrixBuilder.BuilderResult<E>) reader.read();
    }
}
