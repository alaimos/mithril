package com.alaimos.MITHrIL.api.Data.Writer;

import com.alaimos.MITHrIL.api.Commons.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Interface for data writers
 *
 * @param <T> the type of data to write
 */
public interface DataWriterInterface<T> {

    /**
     * Get the file where data are stored
     *
     * @return the file
     */
    File getFile();

    /**
     * Set the filename where data are stored (the file will be created in the application directory)
     *
     * @param f the filename
     * @return this object for a fluent interface
     */
    default DataWriterInterface<T> setFile(String f) {
        setFile(new File(Utils.getAppDir(), f));
        return this;
    }

    /**
     * Set the filename where data are stored (the file will be created in any location)
     *
     * @param f the filename
     * @return this object for a fluent interface
     */
    DataWriterInterface<T> setFile(File f);

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    DataWriterInterface<T> write(T data) throws IOException;

    /**
     * Set filename and read data from it
     *
     * @param f    the filename
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    default DataWriterInterface<T> write(String f, T data) throws IOException {
        return setFile(f).write(data);
    }

    /**
     * Set filename and write data into it
     *
     * @param f    the file
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     * @throws IOException if an I/O error occurs
     */
    default DataWriterInterface<T> write(File f, T data) throws IOException {
        return setFile(f).write(data);
    }

}
