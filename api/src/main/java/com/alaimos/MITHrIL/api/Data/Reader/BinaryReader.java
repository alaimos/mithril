package com.alaimos.MITHrIL.api.Data.Reader;

import com.alaimos.MITHrIL.api.Commons.Utils;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Read an object stored in binary format
 * @param <E> the type of the object to read
 */
public class BinaryReader<E extends Serializable> extends AbstractDataReader<E> {

    private final Class<E> aClass;

    /**
     * Create a new binary reader
     * @param aClass the class of the object to read
     */
    public BinaryReader(Class<E> aClass) {
        this.aClass = aClass;
        isGzipped = true;
    }

    /**
     * Set the file to read
     * @param f the filename (relative to the application directory)
     * @return this object for method chaining
     */
    @Override
    public BinaryReader<E> file(String f) {
        super.file(f);
        isGzipped = true;
        return this;
    }

    /**
     * Set the file to read
     * @param f the file object (absolute path)
     * @return this object for method chaining
     */
    @Override
    public BinaryReader<E> file(File f) {
        super.file(f);
        isGzipped = true;
        return this;
    }

    /**
     * This method does nothing, since the file is always gzipped
     * @param gzipped ignored
     * @return this object for method chaining
     */
    @Override
    public BinaryReader<E> setGzipped(boolean gzipped) {
        return this;
    }

    /**
     * Read the object
     * @return the object
     * @throws IOException if an error occurs
     */
    @Override
    protected E realReader() throws IOException {
        try (ObjectInputStream is = new ObjectInputStream(getInputStream())) {
            return Utils.checkedCast(is.readObject(), aClass);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
