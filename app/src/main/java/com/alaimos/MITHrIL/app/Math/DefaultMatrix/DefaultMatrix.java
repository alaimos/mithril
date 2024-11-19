package com.alaimos.MITHrIL.app.Math.DefaultMatrix;

import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import org.jetbrains.annotations.NotNull;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.RawStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class DefaultMatrix implements MatrixInterface<DefaultMatrix> {

    @Serial
    private static final long serialVersionUID = -8761988017888622755L;

    private transient MatrixR064 internalMatrix;

    private static MatrixR064 byRowMatrix(double[][] matrix) {
        return MatrixR064.FACTORY.makeWrapper(RawStore.wrap(matrix));
    }

    private static MatrixR064 byColumnMatrix(double[][] matrix) {
        return MatrixR064.FACTORY.makeWrapper(RawStore.wrap(matrix).transpose());
    }

    private DefaultMatrix(MatrixR064 matrix) {
        internalMatrix = matrix;
    }

    public DefaultMatrix(double[][] matrix) {
        this(matrix, MatrixInterface.Direction.ROW);
    }

    public DefaultMatrix(double[][] matrix, @NotNull MatrixInterface.Direction direction) {
        internalMatrix = switch (direction) {
            case ROW -> byRowMatrix(matrix);
            case COLUMN -> byColumnMatrix(matrix);
        };
    }

    public DefaultMatrix(double[] matrix, int rows, int columns) {
        var tmpMatrix = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix, i * columns, tmpMatrix[i], 0, columns);
        }
        internalMatrix = byRowMatrix(tmpMatrix);
    }

    public DefaultMatrix(@NotNull MatrixInterface<?> matrix) {
        if (matrix instanceof DefaultMatrix dm) {
            internalMatrix = dm.internalMatrix;
        } else {
            internalMatrix = byRowMatrix(matrix.raw2D());
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

    private MatrixR064 invertInternal() {
        MatrixR064 inverted;
        try {
            var qr = QR.R064.make(internalMatrix);
            inverted = MatrixR064.FACTORY.makeWrapper(qr.invert(internalMatrix));
        } catch (Exception ignored) {
            var svd = SingularValue.R064.make(internalMatrix);
            try {
                inverted = MatrixR064.FACTORY.makeWrapper(svd.invert(internalMatrix));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return inverted;
    }

    /**
     * Invert the matrix. It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     *
     * @return a new matrix
     */
    @Override
    public DefaultMatrix invert() {
        return new DefaultMatrix(invertInternal());
    }

    /**
     * Invert the matrix in place. It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     */
    @Override
    public void invertInPlace() {
        internalMatrix = invertInternal();
    }

    /**
     * Pre-multiply this matrix by another matrix. That is, the operation is performed as matrix * this.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public DefaultMatrix preMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof DefaultMatrix dm) {
            return new DefaultMatrix(dm.internalMatrix.multiply(internalMatrix));
        } else {
            return this.preMultiply(new DefaultMatrix(matrix));
        }
    }

    /**
     * Pre-multiply this matrix by a vector. That is, the operation is performed as vector * this.
     *
     * @param vector the vector
     * @return a new vector
     */
    @Override
    public double[] preMultiply(double[] vector) {
        return MatrixR064.FACTORY.row(vector).multiply(internalMatrix).toRawCopy1D();
    }

    /**
     * Post-multiply this matrix by another matrix. That is, the operation is performed as this * matrix.
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
        return internalMatrix.multiply(MatrixR064.FACTORY.column(vector)).toRawCopy1D();
    }

    /**
     * Subtract a matrix from this matrix
     *
     * @param matrix the matrix
     * @return a new matrix
     */
    @Override
    public DefaultMatrix subtract(MatrixInterface<?> matrix) {
        if (matrix instanceof DefaultMatrix dm) {
            return new DefaultMatrix(internalMatrix.subtract(dm.internalMatrix));
        } else {
            return this.subtract(new DefaultMatrix(matrix));
        }
    }

    /**
     * Given a vector, it returns a new matrix obtained by subtracting the vector from each row or column of the matrix.
     * The direction parameter specifies if the vector is subtracted from rows or columns.
     *
     * @param vector    the vector
     * @param direction the direction
     * @return a new matrix
     */
    @Override
    public DefaultMatrix subtract(double[] vector, Direction direction) {
        var size = direction == Direction.ROW ? rows() : columns();
        double[][] tmp = new double[size][];
        Arrays.fill(tmp, vector);
        var tmpMatrix = switch (direction) {
            case ROW -> byRowMatrix(tmp);
            case COLUMN -> byColumnMatrix(tmp);
        };
        return new DefaultMatrix(internalMatrix.subtract(tmpMatrix));
    }

    /**
     * Subtract a value from each element of the matrix
     *
     * @param value the value
     * @return a new matrix
     */
    @Override
    public DefaultMatrix subtract(double value) {
        return new DefaultMatrix(internalMatrix.subtract(value));
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
        var rows = rows();
        var columns = columns();
        var receiver = new double[rows * columns];
        var tmpMatrix = raw2D();
        for (int i = 0; i < rows; i++) {
            System.arraycopy(tmpMatrix[i], 0, receiver, i * columns, columns);
        }
        return receiver;
    }

    @Override
    public double[] applyFunction(VectorToScalarFunction function, Direction direction) {
        var size = direction == Direction.ROW ? rows() : columns();
        return IntStream.range(0, size)
                        .parallel()
                        .mapToDouble(i -> function.apply(direction == Direction.ROW ? row(i) : column(i), i))
                        .toArray();
    }

    @Override
    public MatrixInterface<?> applyFunction(ElementwiseFunction function) {
        var rows = rows();
        var columns = columns();
        var result = IntStream.range(0, rows)
                              .parallel()
                              .mapToObj(i -> IntStream.range(0, columns)
                                                      .parallel()
                                                      .mapToDouble(j -> function.apply(val(i, j), i, j))
                                                      .toArray())
                              .toArray(double[][]::new);
        return new DefaultMatrix(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MatrixInterface<?> mi)
            return rows() == mi.rows() && columns() == mi.columns() && Arrays.equals(raw1D(), mi.raw1D());
        return false;
    }

    @Override
    public int hashCode() {
        Object tmp = raw1D();
        return Objects.hash(tmp, rows(), columns());
    }

    /**
     * Releases all resources associated with this matrix. For this implementation, it is not necessary to call this
     * method.
     */
    @Override
    public void close() {
        internalMatrix = null;
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
        internalMatrix = byRowMatrix(raw);
    }

}
