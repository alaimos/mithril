package com.alaimos.MITHrIL.app.Data.Records;

import com.alaimos.MITHrIL.api.Data.Encoders.Int2ObjectMapEncoder;
import com.alaimos.MITHrIL.api.Data.Encoders.Object2IntMapEncoder;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
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

public final class PathwayMatrix implements Serializable {
    @Serial
    private static final long serialVersionUID = 3851028820917366684L;
    private final String pathwayId;
    private final MatrixInterface<?> matrix;
    private transient Int2ObjectMap<String> index2Id;
    private transient Object2IntMap<String> id2Index;

    public PathwayMatrix(
            String pathwayId, MatrixInterface<?> matrix, Int2ObjectMap<String> index2Id,
            Object2IntMap<String> id2Index
    ) {
        this.pathwayId = pathwayId;
        this.matrix    = matrix;
        this.index2Id  = index2Id;
        this.id2Index  = id2Index;
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull PathwayMatrix of(
            @NotNull Pathway p, MatrixInterface<?> matrix, Int2ObjectMap<String> index2id,
            Object2IntMap<String> id2index
    ) {
        return new PathwayMatrix(p.id(), matrix, index2id, id2index);
    }

    @Contract("_, _ -> new")
    public static @NotNull PathwayMatrix of(
            @NotNull PathwayMatrix m, @NotNull MatrixFactoryInterface<?> matrixFactory
    ) {
        return new PathwayMatrix(m.pathwayId, matrixFactory.of(m.matrix), m.index2Id, m.id2Index);
    }

    public String pathwayId() {
        return pathwayId;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PathwayMatrix) obj;
        return Objects.equals(this.pathwayId, that.pathwayId) &&
                Objects.equals(this.matrix, that.matrix) &&
                Objects.equals(this.index2Id, that.index2Id) &&
                Objects.equals(this.id2Index, that.id2Index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, matrix, index2Id, id2Index);
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return "PathwayMatrix[" +
                "pathwayId=" + pathwayId + ", " +
                "matrix=" + matrix + ", " +
                "index2Id=" + index2Id + ", " +
                "id2Index=" + id2Index + ']';
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
