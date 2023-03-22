package com.alaimos.MITHrIL.app.Data.Reader;

import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Read MITHrIL input file as a map of gene-expression values
 */
public class ExpressionMapReader extends AbstractDataReader<ExpressionInput> {

    public ExpressionMapReader() {
    }

    public ExpressionMapReader setFile(File f) {
        file = f;
        isGzipped = false;
        return this;
    }

    @Override
    protected ExpressionInput realReader() throws IOException {
        var expressionMap = new Object2DoubleOpenHashMap<String>();
        var nodes = new HashSet<String>();
        try (var r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            String[] s;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty()) continue;
                if (line.startsWith("#")) continue;
                s = line.split("\t", -1);
                if (s.length < 1) continue;
                String node = s[0].trim();
                if (node.isEmpty()) continue;
                nodes.add(node);
                var value = 0.0;
                if (s.length >= 2 && !s[1].equalsIgnoreCase("null") && !s[1].equalsIgnoreCase("na")) {
                    try {
                        value = Double.parseDouble(s[1]);
                    } catch (NumberFormatException | NullPointerException ignored) {
                        value = 0.0;
                    }
                    if (Double.isNaN(value)) value = 0.0;
                }
                if (value != 0.0) expressionMap.put(node, value);
            }
        }
        return new ExpressionInput(nodes.toArray(new String[0]), expressionMap);
    }
}
