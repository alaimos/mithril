package com.alaimos.MITHrIL.app.Data.Pathways;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Records.PathwayMatrix;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.jetbrains.annotations.NotNull;

public class RepositoryMatrixBuilder {

    private final MatrixFactoryInterface<?> factory;

    public RepositoryMatrixBuilder(MatrixFactoryInterface<?> factory) {
        this.factory = factory;
    }

    public RepositoryMatrix build(@NotNull Repository r, @NotNull PathwayMatrix pm) {
        if (!r.contains("metapathway")) return null;
        if (!pm.pathwayId().equals("metapathway")) return null;
        var repositoryIndexes = r.indexMetapathway();
        var pathwayId2Index = repositoryIndexes.right();
        var nodeId2index = pm.id2Index();
        var rows = nodeId2index.size();
        var cols = pathwayId2Index.size();
        var data = new double[rows * cols];
        int i, j;
        for (var vp : r.virtualPathways()) {
            j = pathwayId2Index.getInt(vp.id());
            for (var n : vp.nodes()) {
                i = nodeId2index.getInt(n.id());
                data[i * cols + j] = n.weight();
            }
        }
        var matrix = factory.of(data, rows, cols);
        return RepositoryMatrix.of(matrix, repositoryIndexes.left(), pathwayId2Index, pm);
    }

}
