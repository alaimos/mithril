package com.alaimos.MITHrIL.app.Algorithms.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Stack;

public class ContextualisedMatrixBuilder implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ContextualisedMatrixBuilder.class);
    private final Repository repository;
    private final MatrixFactoryInterface<?> matrixFactory;
    private final RepositoryMatrix originalMatrix;
    private final String[] nonExpressedNodes;
    private final double epsilon;
    private final double minWeight;
    private MatrixInterface<?> contextualisedMatrix = null;
    private final Int2DoubleMap absoluteWeights = new Int2DoubleOpenHashMap();
    private final Long2DoubleMap weights = new Long2DoubleOpenHashMap();
    private double[] attenuationMatrix = null;
    private boolean verbose = true;

    public ContextualisedMatrixBuilder(
            Repository repository, RepositoryMatrix matrix, MatrixFactoryInterface<?> matrixFactory,
            String[] nonExpressedNodes, double epsilon
    ) {
        if (epsilon <= 0) throw new IllegalArgumentException("Epsilon must be positive");
        if (epsilon >= 1) throw new IllegalArgumentException("Epsilon must be less than 1");
        this.repository        = repository;
        this.matrixFactory     = matrixFactory;
        this.originalMatrix    = matrix;
        this.nonExpressedNodes = nonExpressedNodes;
        this.epsilon           = epsilon;
        this.minWeight         = Math.pow(10, -Math.ceil(-Math.log10(epsilon)) - 1);
    }

    public ContextualisedMatrixBuilder verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * Build the matrix representation of a repository
     *
     * @param repository    repository
     * @param matrix        repository matrix
     * @param matrixFactory matrix factory
     * @param epsilon       epsilon
     * @return the matrix representation
     */
    public static MatrixInterface<?> build(
            Repository repository, RepositoryMatrix matrix, MatrixFactoryInterface<?> matrixFactory,
            String[] nonExpressedNodes, double epsilon
    ) {
        return build(repository, matrix, matrixFactory, nonExpressedNodes, epsilon, true);
    }

    /**
     * Build the matrix representation of a repository
     *
     * @param repository    repository
     * @param matrix        repository matrix
     * @param matrixFactory matrix factory
     * @param epsilon       epsilon
     * @return the matrix representation
     */
    public static MatrixInterface<?> build(
            Repository repository, RepositoryMatrix matrix, MatrixFactoryInterface<?> matrixFactory,
            String[] nonExpressedNodes, double epsilon, boolean verbose
    ) {
        var builder = new ContextualisedMatrixBuilder(repository, matrix, matrixFactory, nonExpressedNodes, epsilon);
        builder.verbose(verbose).run();
        return builder.get();
    }

    /**
     * Given a pair of node indices, compute the key for the weight map. The key is a long value, where the first 32
     * bits are the source node index and the last 32 bits are the target node
     *
     * @param u source node
     * @param v target node
     * @return the key
     */
    private long key(int u, int v) {
        return ((long) u << 32) | (v & 0xFFFFFFFFL);
    }

    /**
     * Compute the normalization weight for a column of the matrix
     *
     * @param u a node corresponding to a column of the matrix
     * @return the normalization weight
     */
    private double absoluteTotalWeight(int u) {
        return absoluteWeights.computeIfAbsent(u, s -> {
            var g = repository.get().graph();
            var edges = g.outgoingEdges(g.node(originalMatrix.pathwayMatrix().index2Id().get(u)));
            double weight = 0.0;
            for (Edge d : edges) {
                weight += Math.abs(d.weight());
            }
            return weight;
        });
    }

    /**
     * Compute the weight for a given edge. The weight is negated to avoid subtraction identity matrix in the next
     * steps.
     *
     * @param u source node
     * @param v target node
     * @return the weight
     */
    private double weight(int u, int v) {
        return weights.computeIfAbsent(key(u, v), k -> {
            var index2Id = originalMatrix.pathwayMatrix().index2Id();
            var source = index2Id.get(u);
            var target = index2Id.get(v);
            var weight = -repository.get().graph().edge(source, target).weight() / absoluteTotalWeight(u);
            if (!Double.isFinite(weight)) {
                weight = 0.0;
            }
            return weight;
        });
    }

    /**
     * Starting from a non-expressed node, compute the attenuation of all downstream nodes. The value is directly stored
     * in the attenuation matrix.
     *
     * @param uId  node id
     * @param uIdx node index
     */
    private void nonExpressedVisit(String uId, int uIdx) {
        var g = repository.get().graph();
        var u = g.node(uId);
        if (u == null) return;
        var id2Index = originalMatrix.pathwayMatrix().id2Index();
        var n = id2Index.size();
        var stack = new Stack<DoubleObjectPair<Node>>();
        var visited = new IntOpenHashSet();
        var startIdx = uIdx;
        Node v;
        Collection<Edge> outgoing;
        DoubleObjectPair<Node> tmp;
        int vIdx;
        double w, wT;
        stack.push(DoubleObjectPair.of(1.0 - epsilon, u));
        while (!stack.isEmpty()) {
            tmp  = stack.pop();
            u    = tmp.right();
            uIdx = id2Index.getInt(u.id());
            if (visited.contains(uIdx)) continue;
            w = tmp.leftDouble();
            visited.add(uIdx);
            if (uIdx != startIdx) attenuationMatrix[uIdx * n + startIdx] += w; // Add the weight to the matrix in row uIdx and column startIdx
            outgoing = g.outgoingEdges(u);
            for (var e : outgoing) {
                v    = e.target();
                vIdx = id2Index.getInt(v.id());
                if (u == v || visited.contains(vIdx)) continue;
                // Quantify how much of the perturbation of startIdx is propagated to vIdx
                wT = w * weight(uIdx, vIdx);
                if (Math.abs(wT) < minWeight) continue; // If the weight is too small, we can ignore it
                stack.push(DoubleObjectPair.of(wT, v));
            }
        }
    }

    /**
     * Fill the attenuation matrix with the attenuation values for all non-expressed nodes The matrix is stored as a 1D
     * array, where the value at position (i, j) is stored at position i * n + j where n is the number of nodes in the
     * graph.
     * <p>
     * If u is a non-expressed node, the value at position (u, u) is set to epsilon and all other values in the row are
     * set to 0. The other values are computed by the nonExpressedVisit method, directly as negated normalized weights.
     * In this way, if A_i is the attenuation matrix of non-expressed node i, this method directly computes I - sum(A_i)
     * for all non-expressed nodes i.
     */
    private void fillAttenuationMatrix() {
        var id2Index = originalMatrix.pathwayMatrix().id2Index();
        var n = id2Index.size();
        attenuationMatrix = new double[n * n];
        for (var uId : nonExpressedNodes) {
            var uIdx = id2Index.getInt(uId);
            nonExpressedVisit(uId, uIdx);
        }
        for (var uId : nonExpressedNodes) {
            var uIdx = id2Index.getInt(uId);
            for (var i = 0; i < n; i++) {
                attenuationMatrix[uIdx * n + i] = 0.0;
            }
            attenuationMatrix[uIdx * n + uIdx] = epsilon;
        }
    }

    private void sumIdentityMatrix() {
        var n = originalMatrix.pathwayMatrix().id2Index().size();
        int idx;
        for (var i = 0; i < n; i++) {
            idx = i * n + i;
            if (attenuationMatrix[idx] == 0.0) attenuationMatrix[idx] = 1.0;
        }
    }

    private void computeContextualizedMatrix() {
        var n = originalMatrix.pathwayMatrix().id2Index().size();
        var attenuationMatrixObject = matrixFactory.of(attenuationMatrix, n, n);
        var originalMatrixObject = originalMatrix.pathwayMatrix().matrix();
        contextualisedMatrix = originalMatrixObject.preMultiply(attenuationMatrixObject);
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            if (nonExpressedNodes.length > 0) {
                if (verbose) log.info("Computing attenuation matrix");
                fillAttenuationMatrix();
                sumIdentityMatrix();
                if (verbose) log.info("Computing contextualized matrix");
                computeContextualizedMatrix();
                if (verbose) log.info("Contextualized matrix ready");
            } else {
                if (verbose) log.info("Skipping contextualization: no non-expressed nodes");
                contextualisedMatrix = originalMatrix.pathwayMatrix().matrix();
            }
        } catch (Throwable e) {
            log.error("An error occurred while building the matrix representation", e);
        }
    }

    /**
     * Returns the metapathway
     *
     * @return the metapathway
     */
    public MatrixInterface<?> get() {
        if (contextualisedMatrix == null) run();
        return contextualisedMatrix;
    }
}
