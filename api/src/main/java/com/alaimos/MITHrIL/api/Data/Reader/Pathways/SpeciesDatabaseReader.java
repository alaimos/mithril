package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import com.alaimos.MITHrIL.api.Commons.Constants;
import com.alaimos.MITHrIL.api.Data.Pathways.Species;
import com.alaimos.MITHrIL.api.Data.Reader.DataReaderInterface;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteTextFileReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpeciesDatabaseReader implements DataReaderInterface<Map<String, Species>> {

    public final static SpeciesDatabaseReader INSTANCE = new SpeciesDatabaseReader();

    private SpeciesDatabaseReader() {
    }

    private static RemoteTextFileReader speciesReader() {
        var reader = new RemoteTextFileReader(Constants.INSTANCE.get("species_index_url"), true, Constants.INSTANCE.get("species_index_file"));
        return reader.separator("\t").fieldCountLimit(7);
    }

    private static RemoteTextFileReader reactomeIndexReader() {
        var reader = new RemoteTextFileReader(Constants.INSTANCE.get("reactome_index_url"), true, Constants.INSTANCE.get("reactome_index_file"));
        return reader.separator("\t").fieldCountLimit(2);
    }

    private static @NotNull Map<String, String> buildReactomeMap() throws IOException {
        var reader = reactomeIndexReader();
        var reactomeList = reader.read();
        var map = new HashMap<String, String>();
        for (var s : reactomeList) {
            map.put(s[0], s[1]);
        }
        return map;
    }

    private static @Nullable Species fromStringArray(String @Nullable [] s, Map<String, String> reactomeMap) {
        if (s == null || s.length < 7 || s[0].isEmpty() || s[1].isEmpty()) {
            return null;
        }
        var hasReactome = reactomeMap.containsKey(s[0]);
        var reactomeUrl = reactomeMap.getOrDefault(s[0], "");
        return new Species(s[0], s[1], (Integer.parseInt(s[2]) == 1), (Integer.parseInt(s[3]) == 1), hasReactome, s[4], s[5], s[6], reactomeUrl);
    }

    @Override
    public String file() {
        return Constants.INSTANCE.get("species_index_file");
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
        var reactomeMap = buildReactomeMap();
        var reader = speciesReader();
        var species = reader.read();
        var speciesMap = new HashMap<String, Species>();
        for (var s : species) {
            var ss = fromStringArray(s, reactomeMap);
            if (ss != null) speciesMap.put(ss.id(), ss);
        }
        return speciesMap;
    }
}
