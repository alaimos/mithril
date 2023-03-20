package com.alaimos.MITHrIL.api.Data.Writer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPOutputStream;

/**
 * Write an object in binary format
 *
 * @param <E> the type of the object to write
 */
public class BinaryWriter<E extends Serializable> extends AbstractDataWriter<E> {

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for method chaining
     */
    @Override
    public DataWriterInterface<E> write(E data) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(new GZIPOutputStream(outputStream()))) {
            os.writeObject(data);
        }
        return this;
    }
}
