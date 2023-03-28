package com.alaimos.MITHrIL.app.Algorithms;

import com.alaimos.MITHrIL.api.Data.Pathways.Enrichment.DefaultProbabilityComputation;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.*;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.DefaultEdgeWeightComputationMethod;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.DefaultNodeWeightComputationMethod;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.None;
import com.alaimos.MITHrIL.api.Math.PValue.Combiners.ProductOfP;
import com.alaimos.MITHrIL.api.Math.StreamMedian.ExactMedianComputation;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MatrixBuilderFromMetapathway;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Math.DefaultMatrix.DefaultMatrixFactory;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MITHrILTest {

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

    @Contract(" -> new")
    private @NotNull ExpressionInput generateExpressions() {
        var allNodes = new String[20000];
        allNodes[0] = "a";
        allNodes[1] = "b";
        allNodes[2] = "c";
        allNodes[3] = "d";
        allNodes[4] = "e";
        allNodes[5] = "f";
        var map = new Object2DoubleOpenHashMap<String>();
        map.put("a", 2.0);
        map.put("d", 1.0);
        var i = 1;
        for (; i <= 98; i++) {
            map.put("n" + i, 1.0 + i);
            allNodes[i + 5] = "n" + i;
        }
        for (i = 0; i < allNodes.length; i++) {
            if (allNodes[i] == null) allNodes[i] = "n" + (i - 5);
        }
        return new ExpressionInput(allNodes, map);
    }

    @Contract("_, _ -> new")
    private RepositoryMatrix generateTestRepositoryMatrix(@NotNull Repository r, @NotNull DefaultMatrixFactory f) {
        return MatrixBuilderFromMetapathway.build(r, f);
    }

    @DisplayName("Repository Matrix Builder")
    @Test
    void testRepositoryMatrix() {
        MatrixBuilderFromMetapathway.USE_CACHE = false;
        var r = generateTestRepository();
        var f = new DefaultMatrixFactory();
        var m = generateTestRepositoryMatrix(r, f);
        String[] columns = new String[]{"a", "b", "c", "d", "e", "f"};
        double[][] invertedColumns = new double[][]{
                {
                        0.999999999999997, 0.499999999999993, -0.500000000000002, -0.750000000000001, 0.249999999999992,
                        0.750000000000002
                },
                {
                        -6.89628408617453e-16, 0.999999999999996, -1.09953878538115e-15, -0.5, 0.499999999999995,
                        0.500000000000002
                },
                {3.16191083827172e-15, 6.45619958652527e-15, 1, 1, 9.13400620074293e-15, -1},
                {1.53446474802363e-15, 5.71993653194806e-15, 2.01119941863648e-15, 1, 7.43073754247846e-15, -1},
                {
                        -1.12119200955519e-15, 3.30139205852151e-16, -6.6223889161684e-16, -1.12800221772301e-15,
                        0.999999999999998, -6.15887667181968e-16
                },
                {
                        -8.65829469312285e-16, -3.24536459814438e-15, -6.25877024193972e-16, -8.13538179596315e-18,
                        -4.01491284674669e-15, 1
                }
        };
        var id2Index = m.pathwayMatrix().id2Index();
        var mtx = m.pathwayMatrix().matrix();
        for (var i = 0; i < columns.length; i++) {
            var j = id2Index.getInt(columns[i]);
            double[] column = mtx.column(j);
            double[] expected = invertedColumns[i];
            assertArrayEquals(expected, column, 1e-10);
        }
        var rMtx = m.matrix();
        assertEquals(6, rMtx.rows());
        assertEquals(1, rMtx.columns());
        assertArrayEquals(new double[]{1d, 1d, 1d, 1d, 1d, 1d}, rMtx.column(0), 1e-10);
    }

    @DisplayName("MITHrIL Algorithm")
    @Test
    void testRun() throws IOException {
        MatrixBuilderFromMetapathway.USE_CACHE = false;
        var r = generateTestRepository();
        var f = new DefaultMatrixFactory();
        var m = generateTestRepositoryMatrix(r, f);
        var e = generateExpressions();
        try (var a = new MITHrIL()) {
            a.batchSize(1000)
             .numberOfRepetitions(2000)
             .matrixFactory(f)
             .noPValue(false)
             .medianAlgorithmFactory(ExactMedianComputation::new)
             .probabilityComputation(new DefaultProbabilityComputation())
             .pValueAdjuster(new None())
             .pValueCombiner(new ProductOfP())
             .repository(r)
             .repositoryMatrix(m)
             .input(e)
             .random(new Random(123))
             .run();
            var o = a.output();
            var perturbationsMap = new Object2DoubleOpenHashMap<>(
                    new String[]{"a", "b", "c", "d", "e", "f"}, new double[]{2.0, 1.0, -1.0, -0.5, 0.5, 0.5});
            var accumulatorMap = new Object2DoubleOpenHashMap<>(
                    new String[]{"a", "b", "c", "d", "e", "f"}, new double[]{0.0, 1.0, -1.0, -1.5, 0.5, 0.5});
            var perturbationArray = new double[6];
            var accumulatorArray = new double[6];
            var index2Id = m.pathwayMatrix().index2Id();
            for (var i = 0; i < 6; i++) {
                perturbationArray[i] = perturbationsMap.getDouble(index2Id.get(i));
                accumulatorArray[i]  = accumulatorMap.getDouble(index2Id.get(i));
            }
            assertArrayEquals(perturbationArray, o.nodePerturbations(), 0.1);
            assertArrayEquals(accumulatorArray, o.nodeAccumulators(), 0.1);
            assertEquals(3.664432e-4, o.pathwayProbabilities()[0], 1e-6);
            assertEquals(11.5807, o.pathwayImpactFactors()[0], 1e-4);
            assertEquals(-0.5, o.pathwayAccumulators()[0], 0.1);
            assertEquals(-0.5, o.pathwayCorrectedAccumulators()[0], 0.1);
            assertTrue(o.pathwayPValues()[0] < 0.001);
        }
    }
}