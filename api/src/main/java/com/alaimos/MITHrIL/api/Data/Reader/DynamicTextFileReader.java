package com.alaimos.MITHrIL.api.Data.Reader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Read a text file into a list of strings
 */
public class DynamicTextFileReader extends TextFileReader {

    /**
     * Set the file where data are stored
     *
     * @param f the file object (absolute path)
     * @return this object
     */
    @Override
    public DynamicTextFileReader file(File f) {
        super.file(f);
        isGzipped = file.getName().endsWith(".gz");
        return this;
    }

    @Override
    protected List<String> realReader() throws IOException {
        isGzipped = file.getName().endsWith(".gz");
        return super.realReader();
    }
}
