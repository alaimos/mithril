package com.alaimos.MITHrIL.app.Data.Records;

import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

public record RepositoryMatrix(MatrixInterface<?> matrix, Int2ObjectMap<String> index2Id,
                               Object2IntOpenHashMap<String> id2Index,
                               PathwayMatrix pathwayMatrix) implements Serializable {
    @Serial
    private static final long serialVersionUID = -4986585972222286122L;

    public static @NotNull RepositoryMatrix of(MatrixInterface<?> matrix, Int2ObjectMap<String> index2Id, Object2IntOpenHashMap<String> id2Index, PathwayMatrix pathwayMatrix) {
        return new RepositoryMatrix(matrix, index2Id, id2Index, pathwayMatrix);
    }

    public static @NotNull RepositoryMatrix of(@NotNull RepositoryMatrix m, @NotNull MatrixFactoryInterface<?> matrixFactory) {
        return new RepositoryMatrix(matrixFactory.of(m.matrix()), m.index2Id(), m.id2Index(), PathwayMatrix.of(m.pathwayMatrix(), matrixFactory));
    }
}
