package com.alaimos.MITHrIL.app.Data.Pathways;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Records.PathwayMatrix;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class PathwayMatrixBuilder {

    private final MatrixFactoryInterface<?> factory;
    private final Object2DoubleMap<Node> absoluteWeights = new Object2DoubleOpenHashMap<>();

    public PathwayMatrixBuilder(MatrixFactoryInterface<?> factory) {
        this.factory = factory;
    }

    /**
     * Compute the normalization weight for a column of the matrix
     *
     * @param g a graph
     * @param u a node corresponding to a column of the matrix
     * @return the normalization weight
     */
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
    public PathwayMatrix build(@NotNull Pathway p) {
        absoluteWeights.clear();
        var g = p.graph();
        var indexes = g.index();
        var id2Index = indexes.right();
        var n = id2Index.size();
        var data = new double[n * n];
        Edge e;
        double wT;
        int u, v, i;
        for (var e1 : g.edges().entrySet()) {
            wT = absoluteTotalWeight(g, g.node(e1.getKey()));
            for (var e2 : e1.getValue().entrySet()) {
                e = e2.getValue();
                u = id2Index.getInt(e.source().id());
                v = id2Index.getInt(e.target().id());
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
        return PathwayMatrix.of(p, matrix, indexes.left(), id2Index);
    }

}
