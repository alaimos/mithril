package com.alaimos.MITHrIL.api.Data.Reader;

import java.util.Date;

/**
 * Abstract class for remote data readers
 */
public abstract class AbstractRemoteDataReader<T> extends AbstractDataReader<T> implements RemoteDataReaderInterface<T> {

    protected static long maxTimeCache = 86400; // 1 day
    protected static long limit        = new Date().getTime() - maxTimeCache * 1000;
    protected String  url;
    protected boolean persisted = true;

    static {
        setMaxTimeCache(Long.parseLong(System.getProperty("com.alaimos.MITHrIL.maxTimeCache", "86400")));
    }

    /**
     * Get the maximum time a cached file is valid
     * @return the maximum time in seconds
     */
    public static long getMaxTimeCache() {
        return maxTimeCache;
    }

    /**
     * Set the maximum time a cached file is valid
     * @param maxTimeCache the maximum time in seconds
     */
    public static void setMaxTimeCache(long maxTimeCache) {
        AbstractRemoteDataReader.maxTimeCache = maxTimeCache;
        AbstractRemoteDataReader.limit = new Date().getTime() - maxTimeCache * 1000;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public RemoteDataReaderInterface<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public boolean isPersisted() {
        return this.persisted;
    }

    @Override
    public RemoteDataReaderInterface<T> setPersisted(boolean persisted) {
        this.persisted = persisted;
        return this;
    }

    @Override
    public T read() {
        if (file == null) this.setFile();
        if (!file.exists() || !persisted || (file.lastModified() < limit)) {
            if (url == null || url.isEmpty()) {
                throw new RuntimeException("URL is empty!");
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
