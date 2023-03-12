package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathwayTest {

    private Pathway pathway;

    @BeforeEach
    void setUp() {
        pathway = new Pathway("test", "test pathway", new Graph(), List.of("C1", "C2"));
    }

    @DisplayName("Test pathway fields")
    @Test
    void testFields() {
        assertEquals("test", pathway.id());
        assertEquals("test pathway", pathway.name());
        assertEquals(2, pathway.categories().size());
        assertNotNull(pathway.graph());
        assertEquals(0, pathway.graph().nodes().size());
        assertTrue(pathway.categories().contains("C1"));
        assertTrue(pathway.categories().contains("C2"));
    }

    @Test
    void hasCategory() {
        assertTrue(pathway.hasCategory("C1"));
        assertTrue(pathway.hasCategory("C2"));
        assertFalse(pathway.hasCategory("C3"));
    }
}