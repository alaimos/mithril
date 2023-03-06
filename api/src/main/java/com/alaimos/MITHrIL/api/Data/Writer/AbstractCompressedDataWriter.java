package com.alaimos.MITHrIL.api.Data.Writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Salvatore Alaimo, Ph.D.
 * @version 2.0.0.0
 * @since 06/01/2016
 */
public abstract class AbstractCompressedDataWriter<T> extends AbstractDataWriter<T> implements DataWriterInterface<T> {

    protected OutputStream getOutputStream(boolean append) {
        try {
            return new GZIPOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
