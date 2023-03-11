package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeSubtype;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeDetailTest {

    private EdgeDetail edgeDetail1;
    private EdgeDetail edgeDetail2;
    private EdgeDetail edgeDetail3;

    @BeforeEach
    void setUp() {
        edgeDetail1 = new EdgeDetail("GEREL", "ACTIVATION");
        edgeDetail2 = new EdgeDetail("PPREL", "INHIBITION");
        edgeDetail3 = new EdgeDetail(EdgeType.valueOf("GEREL"), EdgeSubtype.fromString("activation"));
    }

    @DisplayName("Test edge detail fields")
    @Test
    void fieldsTest() {
        assertEquals("EdgeType[name=GEREL]", edgeDetail1.type().toString());
        assertEquals("ACTIVATION", edgeDetail1.subtype().name());
        assertEquals(EdgeType.valueOf("PPREL"), edgeDetail2.type());
        assertEquals(EdgeSubtype.valueOf("ACTIVATION"), edgeDetail3.subtype());
    }

    @Test
    void testEquals() {
        assertEquals(edgeDetail1, edgeDetail3);
        assertNotEquals(edgeDetail1, edgeDetail2);
    }

    @Test
    void testHashCode() {
        assertEquals(edgeDetail1.hashCode(), edgeDetail3.hashCode());
        assertNotEquals(edgeDetail1.hashCode(), edgeDetail2.hashCode());
    }

    @Test
    void testClone() {
        var edgeDetail4 = (EdgeDetail) edgeDetail1.clone();
        var edgeDetail5 = new EdgeDetail(edgeDetail2);
        assertEquals(edgeDetail1, edgeDetail4);
        assertNotSame(edgeDetail1, edgeDetail4);
        assertEquals(edgeDetail2, edgeDetail5);
        assertNotSame(edgeDetail2, edgeDetail5);
    }
}