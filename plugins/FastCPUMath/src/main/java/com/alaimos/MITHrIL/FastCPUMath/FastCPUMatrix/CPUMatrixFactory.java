package com.alaimos.MITHrIL.FastCPUMath.FastCPUMatrix;

import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import org.pf4j.Extension;

@Extension
public class CPUMatrixFactory implements MatrixFactoryInterface<CPUMatrix> {

    /**
     * Get the name of the matrix implementation
     *
     * @return the name
     */
    @Override
    public String name() {
        return "fast-cpu";
    }

    /**
     * Set the maximum number of threads to use for the operations.
     * The operation is SUPPORTED by this implementation, but it is not guaranteed to be respected by the underlying library.
     *
     * @param maxThreads the maximum number of threads
     */
    @Override
    public void setMaxThreads(int maxThreads) {
        // do nothing
    }

    /**
     * Create a new matrix from a 2D array
     *
     * @param matrix the 2D array (array of rows that is the first index is the row number)
     * @return a new matrix
     */
    @Override
    public CPUMatrix of(double[][] matrix) {
        return new CPUMatrix(matrix);
    }

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
    @Override
    public CPUMatrix of(double[] matrix, int rows, int columns) {
        return new CPUMatrix(matrix, rows, columns);
    }

    /**
     * Create a new matrix from a 2D array.
     *
     * @param matrix    the 2D array
     * @param direction the direction of the array (ROW: the array stores the matrix by rows, COLUMN: the array stores the matrix by columns)
     * @return a new matrix
     */
    @Override
    public CPUMatrix of(double[][] matrix, MatrixInterface.Direction direction) {
        return new CPUMatrix(matrix, direction);
    }

    /**
     * Create a new matrix from a matrix of another type.
     * This operation is useful to convert a matrix of a different implementation to a matrix of this implementation.
     * For example, if you have a GPU matrix, and you want to use it on the CPU, you can use this method to convert it.
     *
     * @param matrix the matrix to convert
     * @return a new matrix
     */
    @Override
    public CPUMatrix of(MatrixInterface<?> matrix) {
        return new CPUMatrix(matrix);
    }
}