package com.alaimos.MITHrIL.app.Data.Records;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

public record PathwayMatrix(
        String pathwayId, MatrixInterface<?> matrix, Int2ObjectMap<String> index2Id,
        Object2IntOpenHashMap<String> id2Index
) implements Serializable {
    @Serial
    private static final long serialVersionUID = -33879628955088503L;

    @Contract("_, _, _, _ -> new")
    public static @NotNull PathwayMatrix of(
            Pathway p, MatrixInterface<?> matrix, Int2ObjectMap<String> index2id,
            Object2IntOpenHashMap<String> id2index
    ) {
        return new PathwayMatrix(p.id(), matrix, index2id, id2index);
    }

    @Contract("_, _ -> new")
    public static @NotNull PathwayMatrix of(
            @NotNull PathwayMatrix m, @NotNull MatrixFactoryInterface<?> matrixFactory
    ) {
        return new PathwayMatrix(m.pathwayId, matrixFactory.of(m.matrix), m.index2Id, m.id2Index);
    }
}
