package com.alaimos.MITHrIL.api.Data.Reader;

import java.util.Date;
import java.util.UUID;

/**
 * Common interface for classes that read data from a file that can be downloaded from a remote server
 */
public interface RemoteDataReaderInterface<T> extends DataReaderInterface<T> {

    /**
     * Get the url where data can be found
     *
     * @return the url
     */
    String getUrl();

    /**
     * Set the url where data can be found
     *
     * @param url the url
     * @return this object
     */
    RemoteDataReaderInterface<T> setUrl(String url);

    /**
     * Is the local file temporary, or is it cached for a certain time?
     *
     * @return true, if the local file is cached
     */
    boolean isPersisted();

    /**
     * Set if the local file is temporary or should be cached for a certain time
     *
     * @param persisted true, if the local file should be cached
     * @return this object
     */
    RemoteDataReaderInterface<T> setPersisted(boolean persisted);

    /**
     * Set a temporary filename
     *
     * @return this object
     */
    default DataReaderInterface<T> setFile() {
        return setPersisted(false).setFile("tmp_" + new Date().getTime() + "_" + UUID.randomUUID());
    }

    /**
     * Set and read a URL
     * @param u the url
     * @return the result
     */
    @Override
    default T read(String u) {
        return setUrl(u).read();
    }

    /**
     * Download an URL in a file and reads its content
     * @param u the url
     * @param f the file
     * @return the result
     */
    default T read(String u, String f) {
        return setUrl(u).setFile(f).read();
    }

}
