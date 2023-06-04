package com.alaimos.MITHrIL.app.Algorithms.ReversePhensim;

import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.SetCoveringAlgorithm;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SetCoveringAlgorithmTest {

    @Test
    void run() {
        IntSet universe = IntSets.fromTo(1, 7);
        var subsets = new ArrayList<IntSet>();
        subsets.add(IntSets.fromTo(1, 4));
        subsets.add(IntSets.fromTo(4, 6));
        subsets.add(new IntOpenHashSet(new int[]{1, 4}));
        subsets.add(new IntOpenHashSet(new int[]{2, 3, 5}));
        var solutions = SetCoveringAlgorithm.of(universe, subsets).run();
        var expectedSolutions = new HashSet<IntSet>();
        expectedSolutions.add(new IntRBTreeSet(new int[]{0, 1}));
        expectedSolutions.add(new IntRBTreeSet(new int[]{2, 3}));
        assertEquals(expectedSolutions, solutions);
    }
}