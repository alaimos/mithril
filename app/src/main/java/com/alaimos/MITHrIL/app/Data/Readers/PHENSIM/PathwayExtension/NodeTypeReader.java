package com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.NodeType;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Read a file containing custom node types and returns a list of new types. The types are automatically added to the
 * repository.
 */
public class NodeTypeReader extends AbstractDataReader<String[]> {

    public NodeTypeReader() {
        isGzipped = false;
    }

    @Override
    public NodeTypeReader file(@NotNull File f) {
        file      = f;
        isGzipped = f.getName().endsWith(".gz");
        return this;
    }

    @Override
    protected String[] realReader() throws IOException {
        var typeContainer = new HashSet<String>();
        isGzipped = file.getName().endsWith(".gz");
        try (var r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            String[] s;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                s = line.split("\t", -1);
                if (s.length < 1) continue;
                var type = s[0].trim();
                if (type.isEmpty()) continue;
                if (!typeContainer.add(type)) continue;
                var value = 0.0;
                if (s.length >= 2 && NumberUtils.isCreatable(s[1])) value = NumberUtils.createDouble(s[1]);
                NodeType.add(type, value);
            }
        }
        return typeContainer.toArray(new String[0]);
    }
}
