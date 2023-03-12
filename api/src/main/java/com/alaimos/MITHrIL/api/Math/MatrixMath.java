package com.alaimos.MITHrIL.api.Math;

import org.pf4j.ExtensionPoint;

import java.io.Serializable;

/**
 * This interface defines the methods that a matrix math implementation must implement.
 * A class that implements this interface should be able to perform serialization and deserialization to save the base matrix.
 *
 * @param <M> the matrix type
 * @param <V> the vector type
 */
public interface MatrixMath<M, V> extends ExtensionPoint, Serializable {

    /**
     * Get the name of the matrix math implementation
     *
     * @return the name
     */
    String name();

    /**
     * Set the matrix that will be used as a base for all the other operations.
     * For example, if you want to make several matrix multiplications against the same matrix,
     * in the GPU, you can set the matrix once and then multiply it against other matrices.
     * This is useful to avoid transferring the matrix to the GPU every time you need to multiply it.
     *
     * @param matrix the matrix
     * @param rows   the number of rows
     * @param cols   the number of columns
     */
    void setStoredMatrix(M matrix, int rows, int cols);

    /**
     * Set the maximum number of threads to use for the operations.
     * This operation is not guaranteed to be supported by all implementations.
     * Some implementations may ignore this setting.
     * Refer to the documentation of the implementation you are using.
     *
     * @param maxThreads the maximum number of threads
     */
    void setMaxThreads(int maxThreads);

    /**
     * Invert the base matrix and store the result as the new base matrix.
     * This operation might fail if the base matrix is not set.
     * This operation uses the Moore-Penrose pseudo-inverse method to always return a matrix.
     */
    void invert();

    /**
     * Invert a matrix and return the result.
     * This operation uses the Moore-Penrose pseudo-inverse method to always return a matrix.
     *
     * @param matrix the matrix
     * @param rows   the number of rows
     * @param cols   the number of columns
     * @return the inverted matrix
     */
    M invert(M matrix, int rows, int cols);

    /**
     * Pre-multiply the base matrix by another matrix and return the result.
     *
     * @param matrix the matrix to multiply
     * @param rows   the number of rows
     * @param cols   the number of columns
     * @return the result of the multiplication
     */
    M preMultiply(M matrix, int rows, int cols);

    /**
     * Pre-multiply the base matrix by a vector and return the result.
     *
     * @param vector the vector to multiply
     * @return the result of the multiplication
     */
    V preMultiply(V vector);

    /**
     * Post-multiply the base matrix by another matrix and return the result.
     *
     * @param matrix the matrix to multiply
     * @param rows   the number of rows
     * @param cols   the number of columns
     * @return the result of the multiplication
     */
    M postMultiply(M matrix, int rows, int cols);

    /**
     * Post-multiply the base matrix by a vector and return the result.
     *
     * @param vector the vector to multiply
     * @return the result of the multiplication
     */
    V postMultiply(V vector);

}