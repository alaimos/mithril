package com.alaimos.MITHrIL.api.Data.Reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read a text file into a list of strings
 */
public class TextFileReader extends AbstractDataReader<List<String>> {

    @Override
    protected List<String> realReader() throws IOException {
        var result = new ArrayList<String>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    result.add(line);
                }
            }
        }
        return result;
    }
}
