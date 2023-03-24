package com.alaimos.MITHrIL.api.Data.Writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract class for data writers that compress data
 */
public abstract class AbstractCompressedDataWriter<T> extends AbstractDataWriter<T> implements DataWriterInterface<T> {

    /**
     * Get an output stream for the file
     *
     * @param append if true, the file will be appended if false, the file will be overwritten
     * @return an output stream
     * @throws IOException if an error occurs
     */
    protected OutputStream outputStream(boolean append) throws IOException {
        return new GZIPOutputStream(new FileOutputStream(file));
    }

}
