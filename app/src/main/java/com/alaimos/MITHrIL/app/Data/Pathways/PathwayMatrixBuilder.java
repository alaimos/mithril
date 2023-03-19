package com.alaimos.MITHrIL.app.Data.Pathways;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

public class PathwayMatrixBuilder<E extends MatrixInterface<E>> {

    private final MatrixFactoryInterface<E> factory;
    private final Object2DoubleMap<Node> absoluteWeights = new Object2DoubleOpenHashMap<>();

    public PathwayMatrixBuilder(MatrixFactoryInterface<E> factory) {
        this.factory = factory;
    }

    private double absoluteTotalWeight(Graph g, Node u) {
        if (absoluteWeights.containsKey(u)) return absoluteWeights.getDouble(u);
        double weight = 0.0;
        for (Edge d : g.outgoingEdges(u)) {
            weight += Math.abs(d.weight());
        }
        absoluteWeights.put(u, weight);
        return weight;
    }

    /**
     * Build a matrix for a pathway
     * The matrix (M) is built as follows:
     * - the matrix is square, with n rows and n columns, where n is the number of nodes in the pathway
     * - nodes are mapped to indexes in the matrix, starting from 0
     * - for each edge (u, v) in the pathway, M[v,u] = -w(u, v) / sum(w(u, w) for all nodes w outgoing from u)
     * - the diagonal elements are set to 1.0
     *
     * @param p a pathway
     * @return a pair containing the matrix and a pair containing the indexes of nodes and the reverse map
     */
    public BuilderResult<E> build(@NotNull Pathway p) {
        var indexes = p.graph().index();
        var id2index = indexes.right();
        var n = id2index.size();
        var g = p.graph();
        var data = new double[n * n];
        Edge e;
        double wT;
        int u, v, i;
        for (var e1 : g.edges().entrySet()) {
            wT = absoluteTotalWeight(g, g.node(e1.getKey()));
            for (var e2 : e1.getValue().entrySet()) {
                e = e2.getValue();
                u = id2index.getInt(e.source().id());
                v = id2index.getInt(e.target().id());
                i = v * n + u;
                data[i] = -e.weight() / wT;
                if (!Double.isFinite(data[i])) {
                    data[i] = 0.0;
                }
            }
        }
        for (i = 0; i < n; i++) {
            data[i * n + i] = 1.0;
        }
        var matrix = factory.of(data, n, n);
        return new BuilderResult<>(matrix, indexes.left(), id2index);
    }

    public record BuilderResult<E extends MatrixInterface<E>>(E matrix, Int2ObjectMap<String> index2id,
                                                              Object2IntOpenHashMap<String> id2index) implements Serializable {
        @Serial
        private static final long serialVersionUID = -5989518583031549995L;
    }

}
