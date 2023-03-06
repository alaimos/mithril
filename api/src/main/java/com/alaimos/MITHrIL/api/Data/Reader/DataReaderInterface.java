package com.alaimos.MITHrIL.api.Data.Reader;

import java.io.File;
import java.io.IOException;

/**
 * Common interface for classes that read data from a file
 *
 * @param <T> the type of data to read
 */
public interface DataReaderInterface<T> {

    /**
     * Get the filename where data are stored
     *
     * @return the filename
     */
    String getFile();

    /**
     * Set the filename where data are stored
     *
     * @param f the filename
     * @return this object
     */
    DataReaderInterface<T> setFile(String f);

    /**
     * Set the file where data are stored
     *
     * @param f the filename
     * @return this object
     */
    DataReaderInterface<T> setFile(File f);

    /**
     * Read data
     *
     * @return the result
     * @throws IOException if something goes wrong
     */
    T read() throws IOException;


    /**
     * Set filename and read data from it
     *
     * @param f the filename
     * @return the result
     * @throws IOException if something goes wrong
     */
    default T read(File f) throws IOException {
        return setFile(f).read();
    }

    /**
     * Set filename and read data from it
     *
     * @param f the filename
     * @return the result
     * @throws IOException if something goes wrong
     */
    default T read(String f) throws IOException {
        return setFile(f).read();
    }

}
