package com.alaimos.MITHrIL.api.Data.Writer;

import com.alaimos.Commons.Utils.Utils;

import java.io.File;

/**
 * @author Salvatore Alaimo, Ph.D.
 * @version 2.0.0.0
 * @since 12/12/2015
 */
public interface DataWriterInterface<T> {

    /**
     * Get the file where data are stored
     *
     * @return the file
     */
    File getFile();

    /**
     * Set the filename where data are stored
     *
     * @param f the filename
     * @return this object for a fluent interface
     */
    default DataWriterInterface<T> setFile(String f) {
        setFile(new File(Utils.getAppDir(), f));
        return this;
    }

    /**
     * Set the filename where data are stored
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
     */
    DataWriterInterface<T> write(T data);

    /**
     * Set filename and read data from it
     *
     * @param f    the filename
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    default DataWriterInterface<T> write(String f, T data) {
        return setFile(f).write(data);
    }

    /**
     * Set filename and write data into it
     *
     * @param f    the file
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    default DataWriterInterface<T> write(File f, T data) {
        return setFile(f).write(data);
    }

}
