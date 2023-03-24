package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private Pathway pathway;
    private Repository repository;

    private @NotNull Node generateTestNode(@NotNull String id, @Nullable List<String> aliases) {
        if (aliases == null) aliases = List.of();
        return new Node(id, "node name " + id, "gene", aliases);
    }

    @Contract("_ -> new")
    private @NotNull EdgeDetail generateTestDetail(@NotNull String subType) {
        return new EdgeDetail("gerel", subType);
    }

    @Contract("_, _, _ -> new")
    private @NotNull Edge generateTestEdge(@NotNull Node start, @NotNull Node end, @NotNull String subType) {
        return new Edge(start, end, generateTestDetail(subType));
    }

    @BeforeEach
    void setUp() {
        var node1 = generateTestNode("n1", List.of("A1"));
        var node2 = generateTestNode("n2", List.of("A2"));
        var node3 = generateTestNode("n3", null);
        var node4 = generateTestNode("n4", null);
        var node5 = generateTestNode("n5", null);
        var node6 = generateTestNode("n6", null);
        var node7 = generateTestNode("n7", null);
        var node8 = generateTestNode("n8", null);
        var graph = new Graph();
        graph.addNode(List.of(node1, node2, node3, node4));
        graph.addEdge(List.of(
                generateTestEdge(node1, node2, "activation"),
                generateTestEdge(node2, node3, "activation"),
                generateTestEdge(node3, node4, "activation")
        ));
        graph.setEndpoints(List.of("n4", "n44"));
        var otherGraph = new Graph();
        otherGraph.addNode(List.of(
                node4,
                node5,
                node6,
                node7,
                node8
        ));
        otherGraph.addEdge(List.of(
                generateTestEdge(node4, node5, "activation"),
                generateTestEdge(node5, node6, "inhibition"),
                generateTestEdge(node7, node8, "inhibition")
        ));
        otherGraph.setEndpoints(List.of("n6", "n8"));
        pathway = new Pathway("p1", "pathway 1", graph, List.of("C1", "C2"));
        var otherPathway = new Pathway("p2", "pathway 2", otherGraph, List.of("C2", "C3"));
        repository = new Repository();
        repository.addAll(List.of(pathway, otherPathway));
    }

    @Test
    void addDecoys() {
        repository.addDecoys(10);
        assertEquals(4, repository.size());
        assertEquals(repository.get("p1").graph().countNodes(), repository.get("p1-decoy").graph().countNodes());
        assertEquals(repository.get("p1").graph().countEdges(), repository.get("p1-decoy").graph().countEdges());
        assertEquals(repository.get("p2").graph().countNodes(), repository.get("p2-decoy").graph().countNodes());
        assertEquals(repository.get("p2").graph().countEdges(), repository.get("p2-decoy").graph().countEdges());
    }

    @Test
    void contains() {
        assertTrue(repository.contains("p1"));
        assertTrue(repository.contains("p2"));
        assertFalse(repository.contains("p3"));
        assertTrue(repository.contains(pathway));
        assertFalse(repository.contains(new Pathway("p3", "pathway 3", new Graph(), List.of("C1", "C2"))));
        assertTrue(repository.contains((Object) pathway));
        //noinspection SuspiciousMethodCalls
        assertFalse(repository.contains((Object) "p1"));
    }

    @Test
    void get() {
        assertEquals(pathway, repository.get("p1"));
        assertNull(repository.get("p3"));
        assertNull(repository.get());
    }

    @Test
    void getPathwaysByCategory() {
        assertEquals(List.of(pathway), repository.getPathwaysByCategory("C1"));
        assertEquals(List.of("p1", "p2"), repository.getPathwaysByCategory("C2").stream().map(Pathway::id).toList());
    }

    @Test
    void getPathwayIdsByCategory() {
        assertEquals(List.of("p1"), repository.getPathwayIdsByCategory("C1"));
        assertEquals(List.of("p1", "p2"), repository.getPathwayIdsByCategory("C2"));
    }

    @Test
    void getCategories() {
        var categories = repository.getCategories();
        assertEquals(3, categories.size());
        assertTrue(categories.contains("C1"));
        assertTrue(categories.contains("C2"));
        assertTrue(categories.contains("C3"));
    }

    @Test
    void buildMetapathway() {
        var metapathwayRepository = repository.buildMetapathway(
                new Repository.RepositoryFilter(null, null, null, null, null), false, true);
        assertEquals(1, metapathwayRepository.size());
        assertEquals(metapathwayRepository.get("metapathway"), metapathwayRepository.get());
        assertEquals(8, metapathwayRepository.get().graph().countNodes());
        assertEquals(6, metapathwayRepository.get().graph().countEdges());
        var ep = metapathwayRepository.get().graph().endpoints();
        assertTrue(ep.contains("n4"));
        assertTrue(ep.contains("n6"));
        assertTrue(ep.contains("n8"));
        assertFalse(ep.contains("n44"));
        assertNotNull(metapathwayRepository.virtualPathway("p1"));
        var mp = metapathwayRepository.get().graph();
        var nodes = metapathwayRepository.virtualPathway("p1").nodes();
        assertEquals(4, nodes.size());
        assertTrue(nodes.contains(mp.node("n1")));
        assertTrue(nodes.contains(mp.node("n2")));
        assertTrue(nodes.contains(mp.node("n3")));
        assertTrue(nodes.contains(mp.node("n4")));
    }
}