package com.alaimos.MITHrIL.api.Math;

import java.io.Serializable;
import java.util.function.Function;

/**
 * This interface defines the interface of a nearly immutable matrix with some common operations.
 * The matrix is nearly immutable because it is possible to invert or transpose it in place to save memory.
 * A class that implements this interface should be able to perform serialization and deserialization to save the status.
 */
public interface MatrixInterface<E extends MatrixInterface<?>> extends Serializable {

    /**
     * Transpose the matrix
     *
     * @return a new matrix
     */
    E transpose();

    /**
     * Transpose the matrix in place
     */
    void transposeInPlace();

    /**
     * Invert the matrix.
     * It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     *
     * @return a new matrix
     */
    E invert();

    /**
     * Invert the matrix in place.
     * It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     */
    void invertInPlace();

    /**
     * Pre-multiply this matrix by another matrix.
     * That is, the operation is performed as matrix * this.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    E preMultiply(MatrixInterface<?> matrix);

    /**
     * Pre-multiply this matrix by a vector.
     * That is, the operation is performed as vector * this.
     *
     * @param vector the vector
     * @return a new vector
     */
    double[] preMultiply(double[] vector);

    /**
     * Post-multiply this matrix by another matrix.
     * That is, the operation is performed as this * matrix.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    E postMultiply(MatrixInterface<?> matrix);

    /**
     * Post-multiply this matrix by a vector.
     *
     * @param vector the vector
     * @return a new vector
     */
    double[] postMultiply(double[] vector);

    /**
     * Get the value of a cell
     *
     * @param i the row
     * @param j the column
     * @return the value
     */
    double val(int i, int j);

    /**
     * Get a row of the matrix
     *
     * @param i the row number
     * @return the row
     */
    double[] row(int i);

    /**
     * Get a column of the matrix
     *
     * @param j the column number
     * @return the column
     */
    double[] column(int j);

    /**
     * Get the number of rows
     *
     * @return the number of rows
     */
    int rows();

    /**
     * Get the number of columns
     *
     * @return the number of columns
     */
    int columns();

    /**
     * Apply a function to the matrix.
     * The function is applied to each row or column of the matrix depending on the direction.
     * The function receives a double array and returns a double array.
     *
     * @param function  the function
     * @param direction the direction
     * @return a new matrix
     */
    E apply(Function<double[], double[]> function, Direction direction);

    /**
     * Apply a function to each element of the matrix.
     * The function receives the value of the element, the row and the column indexes.
     *
     * @param function the function
     * @return a new matrix
     */
    E apply(ElementwiseFunction function);

    /**
     * Interface for a function that receives a double value, the row and the column indexes and returns a double value.
     */
    @FunctionalInterface
    interface ElementwiseFunction {
        double apply(double v, int i, int j);
    }

    /**
     * Enum for the direction of the function application
     */
    enum Direction {
        ROW, COLUMN
    }

}
