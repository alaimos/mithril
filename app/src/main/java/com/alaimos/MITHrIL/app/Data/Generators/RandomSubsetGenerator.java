package com.alaimos.MITHrIL.app.Data.Generators;

import java.util.HashSet;
import java.util.Random;

public class RandomSubsetGenerator {

    private final String[] nodes;
    private final Random random;

    public RandomSubsetGenerator(String[] nodes, Random random) {
        this.random = random;
        this.nodes = nodes;
    }

    /**
     * Generate a random subset of nodes
     *
     * @param size the number of nodes in the subset
     * @return the random subset
     */
    public String[] nextSubset(int size) {
        HashSet<String> result = new HashSet<>();
        while (result.size() < size) {
            result.add(nodes[random.nextInt(nodes.length)]);
        }
        return result.toArray(new String[0]);
    }
}
