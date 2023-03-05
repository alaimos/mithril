package com.alaimos.MITHrIL.api.Data.Records;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public abstract class TextFileRecordRepository<R extends Record> extends RecordRepository<R> {
    protected static final Logger log = LoggerFactory.getLogger(TextFileRecordRepository.class);

    protected TextFileRecordRepository() {
        try {
            init();
            read();
        } catch (IOException e) {
            log.error("Error while loading repository", e);
        }
    }

    protected void init() {
    }

    /**
     * Get the file object that contains the records
     *
     * @return the file object
     */
    protected @NotNull File getFileObject() {
        return new File(Utils.getAppDir(), getRecordClass().getSimpleName().replace('.', '_').toLowerCase() + ".enum");
    }

    /**
     * Initialize the repository by reading the file and loading the records.
     *
     * @throws IOException if an error occurs while reading the file
     */
    protected void read() throws IOException {
        File f = getFileObject();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String[] parts;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                parts = line.split("\t", -1);
                if (parts.length == 1) {
                    this.add(parts[0]);
                } else if (parts.length > 1) {
                    this.add(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
                }
            }
        }
    }

}
