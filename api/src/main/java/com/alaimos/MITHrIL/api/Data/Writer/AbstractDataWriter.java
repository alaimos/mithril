package com.alaimos.MITHrIL.api.Data.Writer;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Abstract class for data writers
 */
public abstract class AbstractDataWriter<T> implements DataWriterInterface<T> {

    protected File file;

    /**
     * Get the output stream for the file. If the file exists, it will be overwritten
     *
     * @return the output stream
     * @throws IOException if an I/O error occurs
     */
    protected OutputStream outputStream() throws IOException {
        return outputStream(false);
    }

    /**
     * Get the output stream for the file
     *
     * @param append if true, the file will be appended if false, the file will be overwritten
     * @return the output stream
     * @throws IOException if an I/O error occurs
     */
    protected OutputStream outputStream(boolean append) throws IOException {
        return new FileOutputStream(file, append);
    }

    /**
     * Joins an array of strings in a single string separated by a character separator
     *
     * @param array     the array of strings
     * @param separator the separator
     * @return joined output
     */
    protected String concatArray(String[] array, String separator) {
        if (array == null || array.length == 0) return "";
        return String.join(separator, array);
    }

    /**
     * Joins an array of strings in a single string separated by a character separator
     *
     * @param array     the array of strings
     * @param separator the separator
     * @return joined output
     */
    protected String concatArray(double[] array, String separator) {
        if (array == null || array.length == 0) return "";
        return Arrays.stream(array).mapToObj(Double::toString).collect(Collectors.joining(separator));
    }

    /**
     * Joins an array of strings in a single string separated by a character separator
     *
     * @param array     the array of strings
     * @param separator the separator
     * @return joined output
     */
    protected String concatArray(int[] array, String separator) {
        if (array == null || array.length == 0) return "";
        return Arrays.stream(array).mapToObj(Integer::toString).collect(Collectors.joining(separator));
    }

    /**
     * Join a list of objects in a single string
     *
     * @param array     a list of objects
     * @param separator a separator
     * @return joined output
     */
    protected String concatCollection(Collection<?> array, String separator) {
        if (array == null || array.size() == 0) return "";
        return array.stream().map(Object::toString).collect(Collectors.joining(separator));
    }

    /**
     * Write a set of objects to a file
     *
     * @param ps   a PrintStream object
     * @param data one or more objects to print
     * @return this object for a fluent interface
     */
    protected AbstractDataWriter<T> writeDelimited(PrintStream ps, Object... data) {
        return writeArray(ps, Arrays.stream(data).map(Object::toString).toArray(String[]::new));
    }

    /**
     * Write a set of objects to a file using a specific separator
     *
     * @param ps        a PrintStream Object
     * @param separator a character separator
     * @param data      one or more objects to print
     * @return this object for a fluent interface
     */
    protected AbstractDataWriter<T> writeDelimited(PrintStream ps, String separator, Object... data) {
        return writeArray(ps, Arrays.stream(data).map(Object::toString).toArray(String[]::new), separator);
    }

    /**
     * Writes an array of strings on a PrintStream. Each element in the string array will be concatenated using a tab.
     *
     * @param ps    a PrintStream object
     * @param array an array of strings
     * @return this object for a fluent interface
     */
    protected AbstractDataWriter<T> writeArray(PrintStream ps, String[] array) {
        return writeArray(ps, array, "\t");
    }

    /**
     * Writes an array of strings on a PrintStream. Each element in the string array will be concatenated using a
     * character separator.
     *
     * @param ps        a PrintStream object
     * @param array     an array of strings
     * @param separator a character separator
     * @return this object for a fluent interface
     */
    protected AbstractDataWriter<T> writeArray(@NotNull PrintStream ps, String[] array, String separator) {
        ps.print(concatArray(array, separator));
        return this;
    }

    /**
     * Get the filename where data are stored
     *
     * @return the filename
     */
    @Override
    public File file() {
        return file;
    }

    /**
     * Set the filename where data are stored
     *
     * @param f the filename
     * @return this object
     */
    @Override
    public DataWriterInterface<T> file(File f) {
        this.file = f;
        return this;
    }
}
