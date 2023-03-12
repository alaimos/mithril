package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeSubtype;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    private Node node1;
    private Node node2;
    private Graph graph;
    private Graph otherGraph;

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
        node1 = generateTestNode("n1", List.of("A1"));
        node2 = generateTestNode("n2", List.of("A2"));
        var node3 = generateTestNode("n3", null);
        var node4 = generateTestNode("n4", null);
        var node5 = generateTestNode("n5", null);
        var node6 = generateTestNode("n6", null);
        var node7 = generateTestNode("n7", null);
        var node8 = generateTestNode("n8", null);
        graph = new Graph();
        graph.addNode(List.of(node1, node2, node3, node4));
        graph.addEdge(List.of(
                generateTestEdge(node1, node2, "activation"),
                generateTestEdge(node2, node3, "activation"),
                generateTestEdge(node3, node4, "activation")
        ));
        graph.setEndpoints(List.of("n4", "n44"));
        otherGraph = new Graph();
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
    }

    @Test
    void node() {
        assertEquals(node1, graph.node("n1"));
        assertEquals(node2, graph.node(node2));
    }

    @Test
    void findNode() {
        assertEquals(node1, graph.findNode("n1"));
        assertEquals(node1, graph.findNode("A1"));
        assertEquals(node2, graph.findNode("n2"));
        assertEquals(node2, graph.findNode("A2"));
        assertNull(graph.findNode("a1"));
        assertNull(graph.findNode("a2"));
    }

    @Test
    void removeNode() {
        otherGraph.removeNode("n7");
        assertNull(otherGraph.node("n7"));
    }

    @Test
    void hasNode() {
        assertTrue(graph.hasNode("n1"));
        assertFalse(graph.hasNode("n7"));
    }

    @Test
    void endpoints() {
        assertEquals(List.of("n4"), graph.endpoints());
        assertNotEquals(List.of("n4", "n44"), graph.endpoints());
    }

    @Test
    void inDegree() {
        assertEquals(0, graph.inDegree(node1));
        assertEquals(1, graph.inDegree(node2));
    }

    @Test
    void outDegree() {
        assertEquals(1, graph.outDegree(node1));
        assertEquals(0, graph.outDegree(graph.node("n4")));
    }

    @Test
    void ingoingNodesStream() {
        var list = graph.ingoingNodesStream(node2).map(Stream::toList).orElse(List.of());
        assertEquals(List.of(node1), list);
    }

    @Test
    void outgoingNodesStream() {
        var list = graph.outgoingNodesStream(node1).map(Stream::toList).orElse(List.of());
        assertEquals(List.of(node2), list);
    }

    @Test
    void nodes() {
        assertEquals(4, graph.nodes().size());
    }

    @Test
    void edge() {
        var edge = graph.edge(node1, node2);
        assertNotNull(edge);
        assertEquals(node1, edge.source());
        assertEquals(node2, edge.target());
        assertEquals(EdgeSubtype.fromString("activation"), edge.details().get(0).subtype());
    }

    @Test
    void hasEdge() {
        assertTrue(graph.hasEdge(node1, node2));
        assertFalse(graph.hasEdge(node2, node1));
    }

    @Test
    void edges() {
        assertEquals(4, graph.edges().size());
    }

    @Test
    void countNodes() {
        assertEquals(4, graph.countNodes());
    }

    @Test
    void countEdges() {
        assertEquals(3, graph.countEdges());
    }

    @Test
    void upstreamNodes() {
        var nodes = graph.upstreamNodes(graph.node("n3"));
        assertEquals(List.of(node1, node2), nodes);
    }

    @Test
    void downstreamNodes() {
        var nodes = graph.downstreamNodes(graph.node("n1"));
        assertEquals(List.of(node2, graph.node("n3"), graph.node("n4")), nodes);
    }

    @Test
    void index() {
        var index = graph.index();
        assertEquals(4, index.left().size());
        assertEquals(4, index.right().size());
        assertEquals(node1.id(), index.left().get(index.right().getInt(node1.id())));
    }

    @Test
    void extendWith() {
        graph.extendWith(otherGraph);
        assertEquals(6, graph.countNodes());
        assertEquals(5, graph.countEdges());
        assertFalse(graph.hasNode("n7"));
        assertTrue(graph.hasNode("n6"));
    }

    @Test
    void mergeWith() {
        graph.mergeWith(otherGraph, new Pattern[0], false);
        assertEquals(8, graph.countNodes());
        assertEquals(6, graph.countEdges());
        assertTrue(graph.hasNode("n7"));
        assertTrue(graph.hasNode("n6"));
    }
}