package com.alaimos.MITHrIL.api.Math;

import org.pf4j.ExtensionPoint;

import java.io.IOException;

public interface MatrixFactoryInterface<E extends MatrixInterface<E>> extends ExtensionPoint {

    /**
     * Get the name of the matrix implementation
     *
     * @return the name
     */
    String name();

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
     * Create a new matrix from a 2D array
     *
     * @param matrix the 2D array (array of rows that is the first index is the row number)
     * @return a new matrix
     */
    E of(double[][] matrix);

    /**
     * Create a new matrix from a 1D array.
     * The array stores the matrix by rows.
     * That is, given the element (i,j) of the matrix,
     * the corresponding element in the array is matrix[i * columns + j].
     *
     * @param matrix  the 1D array
     * @param rows    the number of rows
     * @param columns the number of columns
     * @return a new matrix
     */
    E of(double[] matrix, int rows, int columns);

    /**
     * Create a new matrix from a 2D array.
     *
     * @param matrix    the 2D array
     * @param direction the direction of the array (ROW: the array stores the matrix by rows, COLUMN: the array stores the matrix by columns)
     * @return a new matrix
     */
    E of(double[][] matrix, MatrixInterface.Direction direction);

    /**
     * Create a new matrix from a matrix of another type.
     * This operation is useful to convert a matrix of a different implementation to a matrix of this implementation.
     * For example, if you have a GPU matrix, and you want to use it on the CPU, you can use this method to convert it.
     *
     * @param matrix the matrix to convert
     * @return a new matrix
     */
    E of(MatrixInterface<?> matrix);

}
