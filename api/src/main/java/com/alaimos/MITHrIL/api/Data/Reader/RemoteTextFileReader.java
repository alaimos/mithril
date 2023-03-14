package com.alaimos.MITHrIL.api.Data.Reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read a remote text file into a list of string arrays
 */
public class RemoteTextFileReader extends AbstractRemoteDataReader<List<String[]>> {

    protected String separator = "\t";
    protected int fieldCountLimit = -1;

    public RemoteTextFileReader() {
    }

    public RemoteTextFileReader(String url) {
        url(url);
    }


    public RemoteTextFileReader(String url, boolean persisted, String file) {
        persisted(persisted).url(url).file(file);
    }

    public RemoteTextFileReader(String url, String file) {
        this(url, true, file);
    }

    /**
     * Get the separator used to split the lines
     *
     * @return the separator
     */
    public String separator() {
        return separator;
    }

    /**
     * Set the separator used to split the lines
     *
     * @param separator the separator
     * @return this
     */
    public RemoteTextFileReader separator(String separator) {
        this.separator = separator;
        return this;
    }

    /**
     * Get the field count limit
     *
     * @return the field count limit
     */
    public int fieldCountLimit() {
        return fieldCountLimit;
    }

    /**
     * Set the field count limit
     *
     * @param fieldCountLimit the field count limit
     * @return this
     */
    public RemoteTextFileReader fieldCountLimit(int fieldCountLimit) {
        this.fieldCountLimit = fieldCountLimit;
        return this;
    }

    @Override
    protected List<String[]> realReader() throws IOException {
        var result = new ArrayList<String[]>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            String[] s;
            while ((line = r.readLine()) != null) {
                if (!line.isEmpty()) {
                    s = line.split(separator, -1);
                    if (s.length > 0 && (fieldCountLimit < 0 || (fieldCountLimit > 0 && s.length == fieldCountLimit))) {
                        result.add(s);
                    }
                }
            }
        }
        return result;
    }
}
