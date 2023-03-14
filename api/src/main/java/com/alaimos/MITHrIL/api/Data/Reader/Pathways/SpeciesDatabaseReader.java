package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Species;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteTextFileReader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpeciesDatabaseReader implements DataReaderInterface<Map<String, Species>> {

    private final static SpeciesDatabaseReader INSTANCE = new SpeciesDatabaseReader();

    public static SpeciesDatabaseReader getInstance() {
        return INSTANCE;
    }

    private SpeciesDatabaseReader() {
    }

    private static RemoteTextFileReader reader() {
        var reader = new RemoteTextFileReader(Constants.SPECIES_INDEX_URL, true, Constants.SPECIES_INDEX_FILE);
        return reader.separator("\t").fieldCountLimit(7);
    }

    private static @Nullable Species fromStringArray(String @Nullable [] s) {
        if (s == null || s.length < 7 || s[0].isEmpty() || s[1].isEmpty()) {
            return null;
        }
        return new Species(s[0], s[1], (Integer.parseInt(s[2]) == 1), (Integer.parseInt(s[3]) == 1), s[4], s[5], s[6]);
    }

    @Override
    public String file() {
        return Constants.SPECIES_INDEX_URL;
    }

    @Override
    public DataReaderInterface<Map<String, Species>> file(String f) {
        return this;
    }

    @Override
    public DataReaderInterface<Map<String, Species>> file(File f) {
        return this;
    }

    @Override
    public Map<String, Species> read() throws IOException {
        var reader = reader();
        var species = reader.read();
        var speciesMap = new HashMap<String, Species>();
        for (var s : species) {
            var ss = fromStringArray(s);
            if (ss != null) speciesMap.put(ss.id(), ss);
        }
        return speciesMap;
    }
}
