package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    private Node node1;
    private Node node2;
    private Node node3;

    @BeforeEach
    void setUp() {
        node1 = new Node("id1", "name1", "gene", List.of("alias1", "alias2"));
        node2 = new Node("id2", "name2", "mirna", List.of("alias3", "alias4"));
        node3 = new Node("id1", "name1", NodeType.valueOf("GENE"), List.of("alias1", "alias2"));

    }

    @DisplayName("Test node fields")
    @Test
    void fieldsTest() {
        assertEquals("id1", node1.id());
        assertEquals("name1", node1.name());
        assertEquals(NodeType.valueOf("GENE"), node1.type());
        assertEquals(List.of("alias1", "alias2"), node1.aliases());
    }

    @Test
    void testEquals() {
        assertEquals(node1, node3);
        assertNotEquals(node1, node2);
    }

    @Test
    void testHashCode() {
        assertEquals(node1.hashCode(), node3.hashCode());
        assertNotEquals(node1.hashCode(), node2.hashCode());
    }
}