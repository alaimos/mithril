package com.alaimos.MITHrIL.app.Data.Pathways;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Records.PathwayMatrix;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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
     * Build the pathway matrix from an array of weights. The matrix is square, with n rows and n columns, where n is
     * the number of nodes in the pathway. The data array is a flattened matrix, where the element at position i*n+j is
     * the weight of the edge from node j to node i.
     *
     * @param p    a pathway
     * @param data the array of weights
     * @param n    the size of the matrix
     * @return the pathway matrix
     */
    private @NotNull PathwayMatrix matrixFromArray(@NotNull Pathway p, double[] data, int n) {
        var g = p.graph();
        var indexes = g.index();
        for (int i = 0; i < n; i++) {
            data[i * n + i] = 1.0;
        }
        var matrix = factory.of(data, n, n);
        return PathwayMatrix.of(p, matrix, indexes.left(), indexes.right());
    }

    /**
     * Build the weight matrix for a pathway. The matrix (M) is built as follows: - the matrix is square, with n rows
     * and n columns, where n is the number of nodes in the pathway - nodes are mapped to indexes in the matrix,
     * starting from 0 - for each edge (u, v) in the pathway, M[v,u] = -w(u, v) / sum(w(u, w) for all nodes w outgoing
     * from u) - the diagonal elements are set to 1.0
     *
     * @param p a pathway
     * @return the matrix
     */
    private @NotNull PathwayMatrix buildStandardMatrix(@NotNull Pathway p) {
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
                e       = e2.getValue();
                u       = id2Index.getInt(e.source().id());
                v       = id2Index.getInt(e.target().id());
                i       = v * n + u;
                data[i] = -e.weight() / wT;
                if (!Double.isFinite(data[i])) {
                    data[i] = 0.0;
                }
            }
        }
        return matrixFromArray(p, data, n);
    }

    /**
     * Build a customized weight matrix for a pathway. The matrix (M) is built as follows: - the matrix is square, with
     * n rows and n columns, where n is the number of nodes in the pathway - nodes are mapped to indexes in the matrix,
     * starting from 0 - for each edge (u, v) in the pathway, M[v,u] = -w(u, v) / sum(w(u, w) for all nodes w outgoing
     * from u) - the diagonal elements are set to 1.0 - the matrix is customized to the input (unreachable nodes are
     * removed and the matrix is resized)
     *
     * @param p                  a pathway
     * @param customizationNodes a list of nodes to use for customization
     * @return the matrix
     */
    private @NotNull PathwayMatrix buildCustomizedMatrix(
            @NotNull Pathway p,
            @NotNull List<String> customizationNodes
    ) {
        absoluteWeights.clear();
        var g = p.graph();
        var indexes = g.index();
        var id2Index = indexes.right();
        var n = id2Index.size();
        var data = new double[n * n];
        var visited = new HashMap<Node, Color>();
        var stack = new Stack<Node>();
        var predecessors = new HashMap<Node, Node>();
        // First visit the customization nodes
        for (var u : customizationNodes) {
            if (visited.getOrDefault(g.node(u), Color.WHITE) == Color.WHITE) {
                visit(g, g.node(u), visited, stack, predecessors, id2Index, data);
            }
        }
        // Then visit the remaining nodes...these are the unreachable nodes,
        // therefore, their weights should be set to 0...but I check anyway
        for (var u : g.nodes().values()) {
            if (visited.getOrDefault(u, Color.WHITE) == Color.WHITE) {
                visit(g, u, visited, stack, predecessors, id2Index, data);
            }
        }
        return matrixFromArray(p, data, n);
    }

    /**
     * Implementation of a modified DFS algorithm that starting from a node "u" compute the normalized weight matrix. It
     * checks for back-edges, cross-edges, and forward-edges. All cross- and forward-edges are kept. Back-edges in a
     * path u~>v->u are kept only if the sign of the path (computed using the pathSign method) u~>v is different from
     * the sign of the weight of edge v->u.
     *
     * @param g            the graph to visit
     * @param u            The starting point of the visit
     * @param visited      A map containing visited nodes
     * @param stack        The stack used to guide the visit
     * @param predecessors A map giving a predecessor in the DFS tree for each visited node
     * @param id2Index     a map of node id to matrix indexes
     * @param matrix       the matrix
     */
    private void visit(
            @NotNull Graph g,
            @NotNull Node u,
            @NotNull HashMap<Node, Color> visited,
            @NotNull Stack<Node> stack,
            @NotNull HashMap<Node, Node> predecessors,
            @NotNull Object2IntOpenHashMap<String> id2Index,
            double[] matrix
    ) {
        var n = id2Index.size();
        Color cu;
        double w, wT;
        Edge e;
        Node v;
        int uIdx, vIdx, idx;
        List<Edge> outgoing;
        int outgoingSize;
        stack.push(u);
        while (!stack.isEmpty()) {
            u    = stack.peek();
            uIdx = id2Index.getInt(u.id());
            cu   = visited.getOrDefault(u, Color.WHITE);
            wT   = absoluteTotalWeight(g, u);
            if (cu == Color.GRAY) {
                visited.put(u, Color.BLACK);
                stack.pop();
            } else if (cu == Color.WHITE) {
                visited.put(u, Color.GRAY);
                outgoing     = List.copyOf(g.outgoingEdges(u));
                outgoingSize = outgoing.size();
                for (var i = outgoingSize - 1; i >= 0; i--) {
                    e    = outgoing.get(i);
                    v    = e.target();
                    vIdx = id2Index.getInt(v.id());
                    idx  = vIdx * n + uIdx;
                    w    = -e.weight() / wT;
                    if (!Double.isFinite(w)) {
                        w = 0.0;
                    }
                    if (visited.getOrDefault(v, Color.WHITE) == Color.WHITE) {
                        matrix[idx] = w;
                        predecessors.put(v, u);
                        stack.push(v);
                    } else if (visited.getOrDefault(v, Color.WHITE) == Color.BLACK) {
                        matrix[idx] = w;
                    }
                }
            } else {
                var p = predecessors.get(u);
                if (p != null) {
                    w = -g.edge(p, u).weight() / absoluteTotalWeight(g, p);
                    if (!Double.isFinite(w)) {
                        w = 0.0;
                    }
                    vIdx        = id2Index.getInt(p.id());
                    idx         = uIdx * n + vIdx;
                    matrix[idx] = w;
                }
                stack.pop();
            }
        }
    }


    /**
     * Build a matrix for a pathway The matrix (M) is built as follows: - the matrix is square, with n rows and n
     * columns, where n is the number of nodes in the pathway - nodes are mapped to indexes in the matrix, starting from
     * 0 - for each edge (u, v) in the pathway, M[v,u] = -w(u, v) / sum(w(u, w) for all nodes w outgoing from u) - the
     * diagonal elements are set to 1.0
     *
     * @param p                  a pathway
     * @param customizeMatrix    if true, the matrix is customized to the input (unreachable nodes are removed and the
     *                           matrix is resized) if false, the matrix is built as described above
     * @param customizationNodes a list of nodes to use for customization
     * @return a pair containing the matrix and a pair containing the indexes of nodes and the reverse map
     */
    public PathwayMatrix build(@NotNull Pathway p, boolean customizeMatrix, List<String> customizationNodes) {
        if (customizeMatrix && customizationNodes != null && !customizationNodes.isEmpty()) {
            return buildCustomizedMatrix(p, customizationNodes);
        }
        return buildStandardMatrix(p);
    }

    /**
     * Enum that implements colors for the DFS visit
     */
    private enum Color {
        WHITE,
        GRAY,
        BLACK
    }

}
