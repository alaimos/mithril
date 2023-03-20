package com.alaimos.MITHrIL.app.Data.Reader;

import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Read MITHrIL input file as a map of gene-expression values
 */
public class ExpressionMapReader extends AbstractDataReader<Object2DoubleMap<String>> {

    public ExpressionMapReader() {
    }

    public ExpressionMapReader setFile(File f) {
        file = f;
        isGzipped = false;
        return this;
    }

    @Override
    protected Object2DoubleMap<String> realReader() throws IOException {
        var result = new Object2DoubleOpenHashMap<String>();
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
                var value = 0.0;
                if (s.length >= 2 && !s[1].equalsIgnoreCase("null") && !s[1].equalsIgnoreCase("na")) {
                    try {
                        value = Double.parseDouble(s[1]);
                    } catch (NumberFormatException | NullPointerException ignored) {
                        value = 0.0;
                    }
                    if (Double.isNaN(value)) value = 0.0;
                }
                result.put(node, value);
            }
        }
        return result;
    }
}
