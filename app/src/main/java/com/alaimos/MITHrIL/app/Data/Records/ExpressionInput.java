package com.alaimos.MITHrIL.app.Data.Records;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public record ExpressionInput(String[] nodes, Object2DoubleMap<String> expressions) {

    public @NotNull Object2DoubleMap<String> permute(Random random) {
        var permutedExpressions = new Object2DoubleOpenHashMap<String>();
        String no;
        for (double e : expressions.values()) {
            do {
                no = nodes[random.nextInt(nodes.length)];
            } while (permutedExpressions.containsKey(no));
            permutedExpressions.put(no, e);
        }
        return permutedExpressions;
    }
}
