package com.alaimos.MITHrIL.app.Data.Readers;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read a batched MITHrIL input file as a map of experiments-gene-expression values
 */
public class ExpressionBatchReader extends AbstractDataReader<Map<String, ExpressionInput>> {

    public ExpressionBatchReader() {
        isGzipped = false;
    }

    public ExpressionBatchReader setFile(File f) {
        file      = f;
        isGzipped = f.getName().endsWith(".gz");
        return this;
    }

    @Override
    protected Map<String, ExpressionInput> realReader() throws IOException {
        var map = new Int2ObjectOpenHashMap<String>();
        var nodesSet = new HashSet<String>();
        var expressions = new HashMap<String, Object2DoubleMap<String>>();
        isGzipped = file.getName().endsWith(".gz");
        try (var r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            var line = r.readLine();
            if (line == null) throw new RuntimeException("Invalid input file: file is empty");
            var s = line.split("\t", -1);
            for (var i = 0; i < s.length; i++) {
                s[i] = IOUtils.sanitizeFilename(s[i]).trim();
                if (s[i].isEmpty()) throw new RuntimeException("Invalid input file: empty experiment name found");
                map.put((i + 1), s[i]);
                expressions.put(s[i], new Object2DoubleOpenHashMap<>());
            }
            var nextLinesSize = map.size() + 1;
            double value;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty()) continue;
                if (line.startsWith("#")) continue;
                s = line.split("\t", -1);
                if (s.length < nextLinesSize) continue;
                var node = s[0].trim();
                if (node.isEmpty()) continue;
                nodesSet.add(node);
                for (var i = 1; i < s.length; i++) {
                    s[i]  = s[i].trim();
                    value = 0.0;
                    if (!s[i].isEmpty() && !s[i].equalsIgnoreCase("null") && !s[i].equalsIgnoreCase("na")) {
                        try {
                            value = Double.parseDouble(s[i]);
                        } catch (NumberFormatException | NullPointerException ignored) {
                            value = 0.0;
                        }
                        if (Double.isNaN(value)) value = 0.0;
                    }
                    expressions.get(map.get(i)).put(node, value);
                }
            }
        }
        var nodes = nodesSet.toArray(new String[0]);
        return expressions.entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, e -> new ExpressionInput(nodes, e.getValue())));
    }
}
