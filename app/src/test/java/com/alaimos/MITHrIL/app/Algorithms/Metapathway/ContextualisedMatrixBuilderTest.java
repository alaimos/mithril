package com.alaimos.MITHrIL.app.Algorithms.Metapathway;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.*;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.DefaultEdgeWeightComputationMethod;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.DefaultNodeWeightComputationMethod;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Math.DefaultMatrix.DefaultMatrixFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ContextualisedMatrixBuilderTest {

    @Contract("_ -> new")
    private @NotNull Pathway generateTestPathway(Graph g) {
        return new Pathway("testPathway", "pathwayName", g, "category1; category2");
    }

    @Contract("_ -> !null")
    private @NotNull Node generateTestNode(String id) {
        return new Node(id, "node name " + id, "GENE", List.of());
    }

    @Contract("_ -> new")
    private @NotNull EdgeDetail generateTestDescription(String subtype) {
        return new EdgeDetail("gerel", subtype);
    }

    @Contract("_, _, _ -> !null")
    private @NotNull Edge generateTestEdge(Node source, Node destination, String subtype) {
        return new Edge(source, destination, generateTestDescription(subtype));
    }

    @Contract(" -> new")
    private @NotNull Graph generateTestGraph() {
        var nodes = new Node[]{
                generateTestNode("a"), generateTestNode("b"), generateTestNode("c"), generateTestNode("d"),
                generateTestNode("e"), generateTestNode("f")
        };
        var edges = new Edge[]{
                generateTestEdge(nodes[0], nodes[1], "expression"), generateTestEdge(nodes[0], nodes[2], "repression"),
                generateTestEdge(nodes[2], nodes[3], "expression"), generateTestEdge(nodes[1], nodes[4], "expression"),
                generateTestEdge(nodes[1], nodes[3], "repression"), generateTestEdge(nodes[3], nodes[5], "repression")
        };
        Graph g = new Graph();
        for (var n : nodes) g.addNode(n);
        for (var e : edges) g.addEdge(e);
        g.setEndpoints(Arrays.asList("e", "f"));
        return g;
    }

    @Contract(" -> new")
    private Repository generateTestRepository() {
        Edge.setWeightComputationMethod(new DefaultEdgeWeightComputationMethod());
        Node.setWeightComputationMethod(new DefaultNodeWeightComputationMethod());
        Pathway o = generateTestPathway(generateTestGraph());
        Repository r = new Repository();
        r.add(o);
        return r.buildMetapathway(null, false, true);
    }

    @Contract("_, _ -> new")
    private RepositoryMatrix generateTestRepositoryMatrix(@NotNull Repository r, @NotNull DefaultMatrixFactory f) {
        return MatrixBuilderFromMetapathway.build(r, f);
    }

    @Test
    void computeContextualizedMatrix() {
        var repository = generateTestRepository();
        var factory = new DefaultMatrixFactory();
        var repositoryMatrix = generateTestRepositoryMatrix(repository, factory);
        var builder = new ContextualisedMatrixBuilder(
                repository, repositoryMatrix, factory, new String[]{"b"}, 0.001);
        builder.run();
        var mtx = builder.get();
        var vec = new double[]{2.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        var res = mtx.postMultiply(vec);
        assertArrayEquals(new double[]{2.0, 0.001, -1, -1, 0, 2}, res, 0.001);
    }
}