package com.alaimos.MITHrIL.api.Data.Reader;

import com.alaimos.MITHrIL.api.Commons.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Abstract class for data readers
 */
public abstract class AbstractDataReader<T> implements DataReaderInterface<T> {

    protected boolean isGzipped = true;
    protected File file;

    /**
     * Set the filename where data are stored
     * @param f the filename (relative to the application directory)
     * @return this object
     */
    @Override
    public AbstractDataReader<T> setFile(String f) {
        file = new File(Utils.getAppDir(), f);
        return this;
    }

    /**
     * Set the file where data are stored
     * @param f the file object (absolute path)
     * @return this object
     */
    @Override
    public AbstractDataReader<T> setFile(File f) {
        file = f;
        return this;
    }

    /**
     * Get the absolute path of the file where data are stored
     * @return the filename
     */
    @Override
    public String getFile() {
        return file.getAbsolutePath();
    }

    /**
     * Is the file gzipped?
     * @return true, if the file is gzipped
     */
    public boolean isGzipped() {
        return isGzipped;
    }

    /**
     * Set if the file is gzipped
     * @param gzipped true, if the file is gzipped
     * @return this object
     */
    public AbstractDataReader<T> setGzipped(boolean gzipped) {
        isGzipped = gzipped;
        return this;
    }

    /**
     * Get the input stream for the file
     * @return the input stream
     */
    protected InputStream getInputStream() {
        try {
            InputStream f = new FileInputStream(file);
            if (isGzipped) {
                f = new GZIPInputStream(f);
            }
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Real implementation of the method that read data from the file
     * @return the result
     */
    protected abstract T realReader();

    /**
     * Read data from the file
     * @return the result
     */
    @Override
    public T read() {
        if (file == null || (!file.exists())) {
            throw new RuntimeException("Filename is not set or file does not exists.");
        }
        return realReader();
    }
}
