package com.alaimos.MITHrIL.api.Data.Reader;

import com.alaimos.MITHrIL.api.Commons.Utils;

import java.io.IOException;
import java.util.Date;

/**
 * Abstract class for remote data readers
 *
 * @param <T> the type of data to read
 */
public abstract class AbstractRemoteDataReader<T> extends AbstractDataReader<T> implements RemoteDataReaderInterface<T> {

    protected static long maxTimeCache = 86400; // 1 day
    protected static long limit = new Date().getTime() - maxTimeCache * 1000;
    protected String url;
    protected boolean persisted = true;

    static {
        setMaxTimeCache(Long.parseLong(System.getProperty("com.alaimos.MITHrIL.maxTimeCache", "86400")));
    }

    /**
     * Get the maximum time a cached file is valid
     *
     * @return the maximum time in seconds
     */
    public static long getMaxTimeCache() {
        return maxTimeCache;
    }

    /**
     * Set the maximum time a cached file is valid
     *
     * @param maxTimeCache the maximum time in seconds
     */
    public static void setMaxTimeCache(long maxTimeCache) {
        AbstractRemoteDataReader.maxTimeCache = maxTimeCache;
        AbstractRemoteDataReader.limit = new Date().getTime() - maxTimeCache * 1000;
    }

    /**
     * Get the url where data can be found
     *
     * @return the url
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url where data can be found
     *
     * @param url the url
     * @return this object
     */
    @Override
    public RemoteDataReaderInterface<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Is the local file temporary, or is it cached for a certain time?
     *
     * @return true, if the local file is cached
     */
    @Override
    public boolean isPersisted() {
        return this.persisted;
    }

    /**
     * Set if the local file is temporary or should be cached for a certain time
     *
     * @param persisted true, if the local file should be cached
     * @return this object
     */
    @Override
    public RemoteDataReaderInterface<T> setPersisted(boolean persisted) {
        this.persisted = persisted;
        return this;
    }

    /**
     * Read data from a remote file
     *
     * @return this object
     * @throws IOException if something goes wrong
     */
    @Override
    public T read() throws IOException {
        if (file == null) this.setFile();
        if (!file.exists() || !persisted || (file.lastModified() < limit)) {
            if (url == null || url.isEmpty()) {
                throw new IOException("URL is empty!");
            }
            Utils.download(this.url, file);
        }
        T result = realReader();
        if (!persisted) {
            boolean ignore = file.delete();
        }
        return result;
    }
}
