package com.alaimos.MITHrIL.app.Data.Writer;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.BinaryWriter;
import com.alaimos.MITHrIL.api.Data.Writer.DataWriterInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.app.Data.Pathways.PathwayMatrixBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PathwayMatrixWriter<E extends MatrixInterface<E>> implements DataWriterInterface<PathwayMatrixBuilder.BuilderResult<E>> {

    private final BinaryWriter<PathwayMatrixBuilder.BuilderResult<E>> writer = new BinaryWriter<>();

    public PathwayMatrixWriter(@NotNull Repository r, @NotNull Pathway p) {
        writer.setFile("pathway-matrix-" + r.hashCode() + "-" + p.hashCode() + ".bin");
    }

    /**
     * Get the file where data are stored
     *
     * @return the file
     */
    @Override
    public File getFile() {
        return writer.getFile();
    }

    /**
     * Set the filename where data are stored (the file will be created in any location)
     *
     * @param f the filename
     * @return this object for a fluent interface
     */
    @Override
    public DataWriterInterface<PathwayMatrixBuilder.BuilderResult<E>> setFile(File f) {
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
    public DataWriterInterface<PathwayMatrixBuilder.BuilderResult<E>> write(PathwayMatrixBuilder.BuilderResult<E> data) throws IOException {
        writer.write(data);
        return this;
    }
}
