package com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeType;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Read a file containing custom edge types and returns a list of new types. The types are automatically added to the
 * repository.
 */
public class EdgeTypeReader extends AbstractDataReader<String[]> {

    public EdgeTypeReader() {
        isGzipped = false;
    }

    @Override
    public EdgeTypeReader file(@NotNull File f) {
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
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("#")) continue;
                if (!typeContainer.add(line)) continue;
                EdgeType.add(line);
            }
        }
        return typeContainer.toArray(new String[0]);
    }
}
