package com.alaimos.MITHrIL.app.Math.DefaultMatrix;

import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import org.jetbrains.annotations.NotNull;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;

import java.io.*;

public class DefaultMatrix implements MatrixInterface<DefaultMatrix> {

    @Serial
    private static final long serialVersionUID = -8761988017888622755L;

    private transient Primitive64Matrix internalMatrix;

    private DefaultMatrix(Primitive64Matrix matrix) {
        internalMatrix = matrix;
    }

    public DefaultMatrix(double[][] matrix) {
        this(matrix, MatrixInterface.Direction.ROW);
    }

    public DefaultMatrix(double[][] matrix, @NotNull MatrixInterface.Direction direction) {
        internalMatrix = switch (direction) {
            case ROW -> Primitive64Matrix.FACTORY.rows(matrix);
            case COLUMN -> Primitive64Matrix.FACTORY.columns(matrix);
        };
    }

    public DefaultMatrix(double[] matrix, int rows, int columns) {
        var tmpMatrix = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix, i * columns, tmpMatrix[i], 0, columns);
        }
        internalMatrix = Primitive64Matrix.FACTORY.rows(tmpMatrix);
    }

    public DefaultMatrix(@NotNull MatrixInterface<?> matrix) {
        if (matrix instanceof DefaultMatrix dm) {
            internalMatrix = dm.internalMatrix;
        } else {
            internalMatrix = Primitive64Matrix.FACTORY.rows(matrix.raw2D());
        }
    }

    /**
     * Transpose the matrix
     *
     * @return a new matrix
     */
    @Override
    public DefaultMatrix transpose() {
        return new DefaultMatrix(internalMatrix.transpose());
    }

    /**
     * Transpose the matrix in place
     */
    @Override
    public void transposeInPlace() {
        internalMatrix = internalMatrix.transpose();
    }

    private Primitive64Matrix invertInternal() {
        Primitive64Matrix inverted;
        try {
            var qr = QR.PRIMITIVE.make(internalMatrix);
            inverted = Primitive64Matrix.FACTORY.makeWrapper(qr.invert(internalMatrix));
        } catch (Exception ignored) {
            var svd = SingularValue.PRIMITIVE.make(internalMatrix);
            try {
                inverted = Primitive64Matrix.FACTORY.makeWrapper(svd.invert(internalMatrix));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return inverted;
    }

    /**
     * Invert the matrix.
     * It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     *
     * @return a new matrix
     */
    @Override
    public DefaultMatrix invert() {
        return new DefaultMatrix(invertInternal());
    }

    /**
     * Invert the matrix in place.
     * It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     */
    @Override
    public void invertInPlace() {
        internalMatrix = invertInternal();
    }

    /**
     * Pre-multiply this matrix by another matrix.
     * That is, the operation is performed as matrix * this.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public DefaultMatrix preMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof DefaultMatrix dm) {
            return new DefaultMatrix(dm.internalMatrix.multiply(internalMatrix));
        } else {
            return new DefaultMatrix(matrix).preMultiply(this);
        }
    }

    /**
     * Pre-multiply this matrix by a vector.
     * That is, the operation is performed as vector * this.
     *
     * @param vector the vector
     * @return a new vector
     */
    @Override
    public double[] preMultiply(double[] vector) {
        return Primitive64Matrix.FACTORY.row(vector).multiply(internalMatrix).toRawCopy1D();
    }

    /**
     * Post-multiply this matrix by another matrix.
     * That is, the operation is performed as this * matrix.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public DefaultMatrix postMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof DefaultMatrix dm) {
            return new DefaultMatrix(internalMatrix.multiply(dm.internalMatrix));
        } else {
            return this.postMultiply(new DefaultMatrix(matrix));
        }
    }

    /**
     * Post-multiply this matrix by a vector.
     *
     * @param vector the vector
     * @return a new vector
     */
    @Override
    public double[] postMultiply(double[] vector) {
        return internalMatrix.multiply(Primitive64Matrix.FACTORY.column(vector)).toRawCopy1D();
    }

    /**
     * Get the value of a cell
     *
     * @param i the row
     * @param j the column
     * @return the value
     */
    @Override
    public double val(int i, int j) {
        return internalMatrix.doubleValue(i, j);
    }

    /**
     * Get a row of the matrix
     *
     * @param i the row number
     * @return the row
     */
    @Override
    public double[] row(int i) {
        return internalMatrix.row(i).toRawCopy1D();
    }

    /**
     * Get a column of the matrix
     *
     * @param j the column number
     * @return the column
     */
    @Override
    public double[] column(int j) {
        return internalMatrix.column(j).toRawCopy1D();
    }

    /**
     * Get the number of rows
     *
     * @return the number of rows
     */
    @Override
    public int rows() {
        return (int) internalMatrix.countRows();
    }

    /**
     * Get the number of columns
     *
     * @return the number of columns
     */
    @Override
    public int columns() {
        return (int) internalMatrix.countColumns();
    }

    /**
     * Get the raw matrix as a 2D array
     *
     * @return the raw matrix
     */
    @Override
    public double[][] raw2D() {
        return internalMatrix.toRawCopy2D();
    }

    //    /**
//     * Get the raw matrix as a 1D array
//     *
//     * @return the raw matrix
//     */
    public double[] raw1D() {
        return internalMatrix.toRawCopy1D();
    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.raw2D());
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        double[][] raw = (double[][]) ois.readObject();
        internalMatrix = Primitive64Matrix.FACTORY.rows(raw);
    }

    /**
     * Releases all resources associated with this matrix.
     * For this implementation, it is not necessary to call this method.
     */
    @Override
    public void close() {
        internalMatrix = null;
    }
}
