package com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeSubtype;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * Read a file containing custom edge subtypes and returns a list of new subtypes. The subtypes are automatically added
 * to the repository.
 */
public class EdgeSubtypeReader extends AbstractDataReader<String[]> {

    public EdgeSubtypeReader() {
        isGzipped = false;
    }

    @Override
    public EdgeSubtypeReader file(@NotNull File f) {
        file      = f;
        isGzipped = f.getName().endsWith(".gz");
        return this;
    }

    @Override
    protected String[] realReader() throws IOException {
        var subtypeContainer = new HashSet<String>();
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
                if (!subtypeContainer.add(type)) continue;
                var weight = 0d;
                var priority = 0;
                var symbol = "";
                if (s.length >= 2 && NumberUtils.isCreatable(s[1])) weight = NumberUtils.createDouble(s[1]);
                if (s.length >= 3 && NumberUtils.isCreatable(s[2])) priority = NumberUtils.createInteger(s[2]);
                if (s.length >= 4) symbol = s[3];
                EdgeSubtype.add(type, weight, priority, symbol);
            }
        }
        return subtypeContainer.toArray(new String[0]);
    }
}
