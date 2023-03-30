package com.alaimos.MITHrIL.app.Data.Records;

import com.alaimos.MITHrIL.api.Data.Encoders.Int2ObjectMapEncoder;
import com.alaimos.MITHrIL.api.Data.Encoders.Object2IntMapEncoder;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class RepositoryMatrix implements Serializable {
    @Serial
    private static final long serialVersionUID = 4318039760513484999L;
    private final MatrixInterface<?> matrix;
    private final PathwayMatrix pathwayMatrix;
    private transient Int2ObjectMap<String> index2Id;
    private transient Object2IntMap<String> id2Index;

    public RepositoryMatrix(
            MatrixInterface<?> matrix,
            Int2ObjectMap<String> index2Id,
            Object2IntMap<String> id2Index,
            PathwayMatrix pathwayMatrix
    ) {
        this.matrix        = matrix;
        this.index2Id      = index2Id;
        this.id2Index      = id2Index;
        this.pathwayMatrix = pathwayMatrix;
    }

    public static @NotNull RepositoryMatrix of(
            MatrixInterface<?> matrix, Int2ObjectMap<String> index2Id, Object2IntMap<String> id2Index,
            PathwayMatrix pathwayMatrix
    ) {
        return new RepositoryMatrix(matrix, index2Id, id2Index, pathwayMatrix);
    }

    public static @NotNull RepositoryMatrix of(
            @NotNull RepositoryMatrix m, @NotNull MatrixFactoryInterface<?> matrixFactory
    ) {
        return new RepositoryMatrix(
                matrixFactory.of(m.matrix()), m.index2Id(), m.id2Index(),
                PathwayMatrix.of(m.pathwayMatrix(), matrixFactory)
        );
    }

    public MatrixInterface<?> matrix() {
        return matrix;
    }

    public Int2ObjectMap<String> index2Id() {
        return index2Id;
    }

    public Object2IntMap<String> id2Index() {
        return id2Index;
    }

    public PathwayMatrix pathwayMatrix() {
        return pathwayMatrix;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RepositoryMatrix) obj;
        return Objects.equals(this.matrix, that.matrix) &&
                Objects.equals(this.index2Id, that.index2Id) &&
                Objects.equals(this.id2Index, that.id2Index) &&
                Objects.equals(this.pathwayMatrix, that.pathwayMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrix, index2Id, id2Index, pathwayMatrix);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "RepositoryMatrix[" +
                "matrix=" + matrix + ", " +
                "index2Id=" + index2Id + ", " +
                "id2Index=" + id2Index + ", " +
                "pathwayMatrix=" + pathwayMatrix + ']';
    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(Int2ObjectMapEncoder.encode(index2Id));
        stream.writeObject(Object2IntMapEncoder.encode(id2Index));
    }

    @Serial
    private void readObject(java.io.@NotNull ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        index2Id = Int2ObjectMapEncoder.decode(stream.readObject());
        id2Index = Object2IntMapEncoder.decode(stream.readObject());
    }
}
