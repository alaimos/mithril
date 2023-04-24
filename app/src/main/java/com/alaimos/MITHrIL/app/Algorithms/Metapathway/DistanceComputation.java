package com.alaimos.MITHrIL.app.Algorithms.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DistanceComputation implements Runnable {

    private final int[] nodeDistances;
    private final int[] pathwayDistances;
    private final Repository repository;
    private final RepositoryMatrix repositoryMatrix;
    private final String[] sources;

    public DistanceComputation(
            String @NotNull [] sources, @NotNull Repository repository, @NotNull RepositoryMatrix matrix
    ) {
        this.sources          = sources;
        this.repository       = repository;
        this.repositoryMatrix = matrix;
        this.nodeDistances    = new int[matrix.pathwayMatrix().id2Index().size()];
        this.pathwayDistances = new int[matrix.id2Index().size()];
        Arrays.fill(this.nodeDistances, Integer.MAX_VALUE);
        Arrays.fill(this.pathwayDistances, Integer.MAX_VALUE);
    }

    public static Pair<int[], int[]> of(String[] sources, Repository repository, RepositoryMatrix matrix) {
        var d = new DistanceComputation(sources, repository, matrix);
        d.run();
        return d.get();
    }

    private void bfsVisit(String n) {
        var graph = repository.get().graph();
        var index = repositoryMatrix.pathwayMatrix().id2Index();
        var visited = new boolean[index.size()];
        var queue = new ArrayDeque<Node>();
        var node = graph.node(n);
        if (node == null) return;
        var nodeIdx = index.getInt(n);
        visited[nodeIdx]       = true;
        nodeDistances[nodeIdx] = 0;
        queue.add(node);
        while (!queue.isEmpty()) {
            node    = queue.poll();
            nodeIdx = index.getInt(node.id());
            var outgoingEdges = graph.outgoingEdges(node);
            if (outgoingEdges == null) continue;
            for (var edge : outgoingEdges) {
                var target = edge.target();
                var targetIdx = index.getInt(target.id());
                if (visited[targetIdx]) continue;
                visited[targetIdx]       = true;
                nodeDistances[targetIdx] = Math.min(nodeDistances[targetIdx], nodeDistances[nodeIdx] + 1);
                queue.add(target);
            }
        }
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        for (var s : sources) {
            bfsVisit(s);
        }
        var pathwayId2Index = repositoryMatrix.id2Index();
        var nodeId2Index = repositoryMatrix.pathwayMatrix().id2Index();
        for (var p : repository.virtualPathways()) {
            var pIdx = pathwayId2Index.getInt(p.id());
            for (var n : p.nodes()) {
                var nIdx = nodeId2Index.getInt(n.id());
                pathwayDistances[pIdx] = Math.min(pathwayDistances[pIdx], nodeDistances[nIdx]);
            }
        }
    }

    /**
     * Returns the metapathway
     *
     * @return the metapathway
     */
    public Pair<int[], int[]> get() {
        return Pair.of(nodeDistances, pathwayDistances);
    }
}
