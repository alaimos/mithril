package com.alaimos.MITHrIL.api.Data.Reader;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * Common interface for classes that read data from a file that can be downloaded from a remote server
 *
 * @param <T> the type of data to read
 */
public interface RemoteDataReaderInterface<T> extends DataReaderInterface<T> {

    /**
     * Get the url where data can be found
     *
     * @return the url
     */
    String url();

    /**
     * Set the url where data can be found
     *
     * @param url the url
     * @return this object
     */
    RemoteDataReaderInterface<T> url(String url);

    /**
     * Is the local file temporary, or is it cached for a certain time?
     *
     * @return true, if the local file is cached
     */
    boolean persisted();

    /**
     * Set if the local file is temporary or should be cached for a certain time
     *
     * @param persisted true, if the local file should be cached
     * @return this object
     */
    RemoteDataReaderInterface<T> persisted(boolean persisted);

    /**
     * Set a temporary filename
     */
    default void temporaryFile() {
        persisted(false).file("tmp_" + new Date().getTime() + "_" + UUID.randomUUID());
    }

    /**
     * Set and read a URL
     *
     * @param u the url
     * @return the result
     * @throws IOException if something goes wrong
     */
    @Override
    default T read(String u) throws IOException {
        return url(u).read();
    }

    /**
     * Download a URL in a file and reads its content
     *
     * @param u the url
     * @param f the file
     * @return the result
     * @throws IOException if something goes wrong
     */
    default T read(String u, String f) throws IOException {
        return url(u).file(f).read();
    }

}
