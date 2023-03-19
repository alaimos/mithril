package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.DefaultEdgeWeightComputationMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EdgeTest {

    private Node node1;
    private Node node2;
    private Edge edge1;
    private Edge edge2;
    private Edge edge3;


    @BeforeEach
    void setUp() {
        node1 = new Node("node1", "node1", "gene", List.of());
        node2 = new Node("node2", "node2", "gene", List.of());
        Node node3 = new Node("node3", "node3", "gene", List.of());
        edge1 = new Edge(node1, node2, new EdgeDetail("pprel", "activation"));
        edge2 = new Edge(node1, node2, new EdgeDetail("pprel", "activation"));
        edge3 = new Edge(node1, node3, List.of(new EdgeDetail("pprel", "missing_interaction"), new EdgeDetail("pprel", "inhibition")));
        Edge.setWeightComputationMethod(new DefaultEdgeWeightComputationMethod());
    }

    @DisplayName("Test edge fields")
    @Test
    void source() {
        assertEquals(node1, edge1.source());
        assertEquals(node2, edge1.target());
        assertEquals(List.of(new EdgeDetail("pprel", "activation")), edge1.details());
        assertFalse(edge1.isMultiEdge());
        assertTrue(edge3.isMultiEdge());
        assertEquals(1, edge1.weight(), 0.0001);
        assertEquals(-1, edge3.weight(), 0.0001);
    }

    @Test
    void merge() {
        var e = new Edge(node1, node2, new EdgeDetail("pprel", "activation"));
        e.mergeWith(edge1, true, true);
        assertEquals(List.of(new EdgeDetail("pprel", "activation"), new EdgeDetail("pprel", "activation")), e.details());
        assertEquals(1, e.weight(), 0.0001);
        e = new Edge(node1, node2, new EdgeDetail("pprel", "activation"));
        e.mergeWith(edge1, true, false);
        assertEquals(List.of(new EdgeDetail("pprel", "activation")), e.details());
        assertEquals(1, e.weight(), 0.0001);
        e = new Edge(node1, node2, new EdgeDetail("pprel", "missing_interaction"));
        e.mergeWith(edge1, true, false);
        assertEquals(List.of(new EdgeDetail("pprel", "missing_interaction")), e.details());
        assertEquals(0, e.weight(), 0.0001);
    }

    @Test
    void testEquals() {
        assertEquals(edge1, edge2);
        assertNotEquals(edge1, edge3);
        assertNotEquals(edge1, null);
        assertNotEquals(edge1, new Object());
    }

    @Test
    void testHashCode() {
        assertEquals(edge1.hashCode(), edge2.hashCode());
        assertNotEquals(edge1.hashCode(), edge3.hashCode());
    }

    @Test
    void testClone() {
        var e = (Edge) edge1.clone();
        var e2 = new Edge(edge1);
        assertEquals(edge1, e);
        assertEquals(edge1, e2);
        assertNotSame(edge1, e);
        assertNotSame(edge1, e2);
        assertSame(edge1.source(), e.source());
        assertSame(edge1.source(), e2.source());
        assertSame(edge1.target(), e.target());
        assertSame(edge1.target(), e2.target());
        assertEquals(edge1.details(), e.details());
        assertNotSame(edge1.details(), e.details());
    }
}