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

    /**
     * Convert an array of double values to a vector.
     *
     * @param values the values
     * @return the vector
     */
    V buildVector(double... values);

    /**
     * Convert an iterable of vector elements to a vector.
     *
     * @param size   the size of the vector
     * @param values the values
     * @return the vector
     */
    V buildVector(int size, Iterable<VectorElement> values);

    /**
     * Use a generator to build a vector.
     *
     * @param size      the size of the vector
     * @param generator the generator
     * @return the vector
     */
    V buildVector(int size, VectorElementGenerator generator);

    /**
     * Convert an array of MatrixElement to a matrix.
     *
     * @param rows     the number of rows
     * @param cols     the number of columns
     * @param elements the elements
     * @return the matrix
     */
    M buildMatrix(int rows, int cols, MatrixElement... elements);

    /**
     * Convert an iterable of MatrixElement to a matrix.
     *
     * @param rows     the number of rows
     * @param cols     the number of columns
     * @param elements the elements
     * @return the matrix
     */
    M buildMatrix(int rows, int cols, Iterable<MatrixElement> elements);

    /**
     * Use a generator to build a matrix.
     *
     * @param rows      the number of rows
     * @param cols      the number of columns
     * @param generator the generator
     * @return the matrix
     */
    M buildMatrix(int rows, int cols, MatrixElementGenerator generator);

    /**
     * A lightweight record to store a matrix element.
     * Used to build a new matrix from a list of elements.
     * Note that no check is performed on the values of i and j.
     *
     * @param i the row index
     * @param j the column index
     * @param v the value
     */
    record MatrixElement(int i, int j, double v) {
        @Override
        public String toString() {
            return "<" + i + ", " + j + ", " + v + '>';
        }
    }

    /**
     * A functional interface to generate matrix elements.
     */
    @FunctionalInterface
    interface MatrixElementGenerator {
        double apply(int i, int j);
    }

    /**
     * A lightweight record to store a vector element.
     * Note that no check is performed on the value of i.
     *
     * @param i the index
     * @param v the value
     */
    record VectorElement(int i, double v) {
        @Override
        public String toString() {
            return "<" + i + ", " + v + '>';
        }
    }

    /**
     * A functional interface to generate vector elements.
     */
    @FunctionalInterface
    interface VectorElementGenerator {
        double apply(int i);
    }
}
