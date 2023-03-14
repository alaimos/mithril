package com.alaimos.MITHrIL.api.Data.Reader;

import com.alaimos.MITHrIL.api.Commons.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Abstract class for data readers
 *
 * @param <T> the type of data to read
 */
public abstract class AbstractDataReader<T> implements DataReaderInterface<T> {

    protected boolean isGzipped = true;
    protected File file;

    /**
     * Set the filename where data are stored
     *
     * @param f the filename (relative to the application directory)
     * @return this object
     */
    @Override
    public AbstractDataReader<T> file(String f) {
        file = new File(Utils.getAppDir(), f);
        return this;
    }

    /**
     * Set the file where data are stored
     *
     * @param f the file object (absolute path)
     * @return this object
     */
    @Override
    public AbstractDataReader<T> file(File f) {
        file = f;
        return this;
    }

    /**
     * Get the absolute path of the file where data are stored
     *
     * @return the filename
     */
    @Override
    public String file() {
        return file.getAbsolutePath();
    }

    /**
     * Is the file gzipped?
     *
     * @return true, if the file is gzipped
     */
    public boolean isGzipped() {
        return isGzipped;
    }

    /**
     * Set if the file is gzipped
     *
     * @param gzipped true, if the file is gzipped
     * @return this object
     */
    public AbstractDataReader<T> setGzipped(boolean gzipped) {
        isGzipped = gzipped;
        return this;
    }

    /**
     * Get the input stream for the file
     *
     * @return the input stream
     * @throws IOException if something goes wrong
     */
    protected InputStream getInputStream() throws IOException {
        InputStream f = new FileInputStream(file);
        if (isGzipped) {
            f = new GZIPInputStream(f);
        }
        return f;
    }

    /**
     * Real implementation of the method that read data from the file
     *
     * @return the result
     * @throws IOException if something goes wrong
     */
    protected abstract T realReader() throws IOException;

    /**
     * Read data from the file
     *
     * @return the result
     * @throws IOException if something goes wrong
     */
    @Override
    public T read() throws IOException {
        if (file == null || (!file.exists())) {
            throw new IOException("Filename is not set or file does not exists.");
        }
        return realReader();
    }
}
