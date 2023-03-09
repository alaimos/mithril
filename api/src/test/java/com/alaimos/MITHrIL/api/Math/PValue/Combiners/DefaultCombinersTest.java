package com.alaimos.MITHrIL.api.Math.PValue.Combiners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCombinersTest {

    protected static final double[] FIRST = new double[]{0.0001, 0.0001, 0.9999, 0.9999};
    protected static final double[] SECOND = new double[]{0.016, 0.067, 0.250, 0.405, 0.871};

    @Test
    public void testFisher() {
        var combiner = new Fisher();
        assertTrue(combiner.combine(FIRST) < 0.01);
        assertFalse(combiner.combine(SECOND) < 0.01);
    }

    @Test
    public void testStouffer() {
        var combiner = new Stouffer();
        assertFalse(combiner.combine(FIRST) < 0.1);
        assertTrue(combiner.combine(SECOND) < 0.1);
    }

    @Test
    public void testMean() {
        var combiner = new Mean();
        assertFalse(combiner.combine(FIRST) < 0.1);
        assertTrue(combiner.combine(SECOND) < 0.1);
    }

    @Test
    public void testLogit() {
        var combiner = new Logit();
        assertFalse(combiner.combine(FIRST) < 0.1);
        assertTrue(combiner.combine(SECOND) < 0.06);
    }

    @Test
    public void testWilkinson() {
        var combiner = new Wilkinson();
        assertTrue(combiner.combine(FIRST) < 0.001);
        assertFalse(combiner.combine(SECOND) < 0.05);
    }

    @Test
    public void testSumOfP() {
        var combiner = new SumOfP();
        assertFalse(combiner.combine(FIRST) < 0.1);
        assertTrue(combiner.combine(SECOND) < 0.1);
    }

    @Test
    public void testVoteCounting() {
        var combiner = new VoteCounting();
        assertEquals(0.6875, combiner.combine(FIRST), 0.001);
        assertEquals(0.1875, combiner.combine(SECOND), 0.001);
    }

    @Test
    public void testTwoSidedToOneSided() {
        assertEquals(0.025, CombinerInterface.twoSidedToOneSided(0.05, false), 0.0001);
        assertEquals(0.975, CombinerInterface.twoSidedToOneSided(0.05, true), 0.0001);
    }

}