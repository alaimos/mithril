package com.alaimos.MITHrIL.FastCPUMath.FastCPUMatrix;

import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.pytorch.Scalar;
import org.bytedeco.pytorch.Tensor;
import org.bytedeco.pytorch.global.torch;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.bytedeco.pytorch.global.torch.dtype;
import static org.bytedeco.pytorch.global.torch.zeros;

public class CPUMatrix implements MatrixInterface<CPUMatrix> {

    @Serial
    private static final long serialVersionUID = -8761988017888622755L;

    private transient Tensor tensor;

    private transient DoublePointer pointer = null;

    private transient double[] data = null;

    private int rows;

    private int columns;

    private CPUMatrix(Tensor matrix, int rows, int columns) {
        tensor       = matrix;
        this.rows    = rows;
        this.columns = columns;
    }

    public CPUMatrix(double[][] matrix) {
        this(matrix, Direction.ROW);
    }

    public CPUMatrix(double[][] matrix, @NotNull MatrixInterface.Direction direction) {
        this(
                switch (direction) {
                    case ROW -> vectorFromMatrixByRow(matrix);
                    case COLUMN -> vectorFromMatrixByColumn(matrix);
                }, switch (direction) {
                    case ROW -> matrix.length;
                    case COLUMN -> matrix[0].length;
                }, switch (direction) {
                    case ROW -> matrix[0].length;
                    case COLUMN -> matrix.length;
                }
        );
    }

    public CPUMatrix(double[] matrix, int rows, int columns) {
        tensor       = tensorFromArray(matrix, new long[]{rows, columns});
        this.rows    = rows;
        this.columns = columns;
    }

    public CPUMatrix(@NotNull MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            tensor  = dm.tensor;
            rows    = dm.rows;
            columns = dm.columns;
        } else {
            tensor  = tensorFromArray(matrix.raw1D(), new long[]{matrix.rows(), matrix.columns()});
            rows    = matrix.rows();
            columns = matrix.columns();
        }
    }

    private static double @NotNull [] vectorFromMatrixByRow(double @NotNull [] @NotNull [] matrix) {
        var rows = matrix.length;
        var cols = matrix[0].length;
        var data = new double[rows * cols];
        for (var i = 0; i < rows; i++) {
            System.arraycopy(matrix[i], 0, data, i * cols, cols);
        }
        return data;
    }

    private static double @NotNull [] vectorFromMatrixByColumn(double @NotNull [] @NotNull [] matrix) {
        var rows = matrix[0].length;
        var cols = matrix.length;
        var data = new double[rows * cols];
        for (var i = 0; i < rows; i++) {
            for (var j = 0; j < cols; j++) {
                data[i * cols + j] = matrix[j][i];
            }
        }
        return data;
    }

    private static Tensor tensorFromArray(double @NotNull [] array, long @NotNull [] shape) {
        var tensor = zeros(shape, dtype(torch.ScalarType.Double));
        try (var ptr = tensor.data_ptr_double()) {
            ptr.put(array);
        }
        return tensor;
    }

    private static double @NotNull [] tensorToArray(@NotNull Tensor tensor) {
        var array = new double[(int) tensor.numel()];
        try (var ptr = tensor.data_ptr_double()) {
            ptr.get(array);
        }
        return array;
    }

    /**
     * Transpose the matrix
     *
     * @return a new matrix
     */
    @Override
    public CPUMatrix transpose() {
        try (var tmp = tensor.t()) {
            return new CPUMatrix(tmp.contiguous(), columns, rows);
        }
    }

    /**
     * Transpose the matrix in place
     */
    @Override
    public void transposeInPlace() {
        closePointer();
        try (var tmp = tensor.t()) {
            tensor.close();
            tensor = tmp.contiguous();
        }
        swapSize();
    }

    /**
     * Invert the matrix. It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     *
     * @return a new matrix
     */
    @Override
    public CPUMatrix invert() {
        return new CPUMatrix(tensor.pinverse(), columns, rows);
    }

    /**
     * Invert the matrix in place. It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     */
    @Override
    public void invertInPlace() {
        closePointer();
        var oldInternal = tensor;
        tensor = tensor.pinverse();
        oldInternal.close();
        // The pseudo-inverse of an m-by-n rectangular matrix is an n-by-m matrix
        swapSize();
    }

    @Override
    public int rank() {
        try (var rank = torch.linalg_matrix_rank(tensor)) {
            return rank.item_int();
        }
    }

    @Override
    public double determinant() {
        try (var determinant = tensor.det()) {
            return determinant.item_double();
        }
    }

    /**
     * Check if the matrix is invertible
     *
     * @return true if the matrix is invertible
     */
    @Override
    public boolean invertible() {
        return Math.abs(determinant()) > 1e-12;
    }

    /**
     * Pre-multiply this matrix by another matrix. That is, the operation is performed as matrix * this.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public CPUMatrix preMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            var rows = dm.rows();
            return new CPUMatrix(dm.tensor.matmul(tensor), rows, columns);
        } else {
            return this.preMultiply(new CPUMatrix(matrix));
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
        try (
                var vectorTensor = tensorFromArray(vector, new long[]{1, vector.length});
                var result = vectorTensor.matmul(tensor)
        ) {
            return tensorToArray(result);
        }
    }

    /**
     * Post-multiply this matrix by another matrix. That is, the operation is performed as this * matrix.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public CPUMatrix postMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            var columns = dm.columns();
            return new CPUMatrix(tensor.matmul(dm.tensor), rows, columns);
        } else {
            return this.postMultiply(new CPUMatrix(matrix));
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
        try (
                var vectorTensor = tensorFromArray(vector, new long[]{vector.length, 1});
                var result = tensor.matmul(vectorTensor)
        ) {
            return tensorToArray(result);
        }
    }

    /**
     * Subtract a matrix from this matrix
     *
     * @param matrix the matrix
     * @return a new matrix
     */
    @Override
    public CPUMatrix subtract(MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            return new CPUMatrix(tensor.sub(dm.tensor), rows, columns);
        } else {
            return new CPUMatrix(matrix).subtract(this);
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
    public CPUMatrix subtract(double[] vector, Direction direction) {
        var vectorTensorSize = direction == Direction.ROW ? new long[]{1, vector.length} : new long[]{vector.length, 1};
        try (var vectorTensor = tensorFromArray(vector, vectorTensorSize)) {
            return new CPUMatrix(tensor.sub(vectorTensor), rows, columns);
        }
    }

    /**
     * Subtract a value from each element of the matrix
     *
     * @param value the value
     * @return a new matrix
     */
    @Override
    public CPUMatrix subtract(double value) {
        try (var scalar = new Scalar(value)) {
            return new CPUMatrix(tensor.sub(scalar), rows, columns);
        }
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
        openPointer();
        return data[i * columns + j];
    }

    /**
     * Get a row of the matrix
     *
     * @param i the row number
     * @return the row
     */
    @Override
    public double[] row(int i) {
        openPointer();
        var row = new double[columns];
        System.arraycopy(data, i * columns, row, 0, columns);
        return row;
    }

    /**
     * Get a column of the matrix
     *
     * @param j the column number
     * @return the column
     */
    @Override
    public double[] column(int j) {
        openPointer();
        var column = new double[rows];
        for (var i = 0; i < rows; i++) {
            column[i] = data[i * columns + j];
        }
        return column;
    }

    /**
     * Get the number of rows
     *
     * @return the number of rows
     */
    @Override
    public int rows() {
        return rows;
    }

    /**
     * Get the number of columns
     *
     * @return the number of columns
     */
    @Override
    public int columns() {
        return columns;
    }

    /**
     * Get the raw matrix as a 2D array
     *
     * @return the raw matrix
     */
    @Override
    public double[][] raw2D() {
        openPointer();
        var dataMatrix = new double[rows][columns];
        for (var i = 0; i < rows; i++) {
            System.arraycopy(data, i * columns, dataMatrix[i], 0, columns);
        }
        return dataMatrix;
    }

    /**
     * Get the raw matrix as a 1D array
     *
     * @return the raw matrix
     */
    @Override
    public double[] raw1D() {
        openPointer();
        return data;
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
                                                      .mapToDouble(j -> function.apply(val(i, j), i, j)))
                              .flatMapToDouble(Function.identity())
                              .toArray();
        return new CPUMatrix(result, rows, columns);
//
//        double[] result = new double[rows * columns];
//        for (var i = 0; i < rows; i++) {
//            for (var j = 0; j < columns; j++) {
//                result[i * columns + j] = function.apply(val(i, j), i, j);
//            }
//        }
//        return new CPUMatrix(result, rows, columns);
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
     * Releases the tensor and its backing memory.
     */
    @Override
    public void close() {
        closePointer();
        tensor.close();
    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(raw1D());
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        double[] raw = (double[]) ois.readObject();
        tensor = tensorFromArray(raw, new long[]{rows, columns});
    }

    /**
     * Open a pointer to the underlying pytorch tensor and preload the data into a double array
     */
    private void openPointer() {
        if (pointer == null) {
            pointer = tensor.data_ptr_double();
            data    = new double[rows * columns];
            pointer.get(data);
        }
    }

    /**
     * Close the pointer to the underlying pytorch tensor and free the double array
     */
    private void closePointer() {
        if (pointer != null) {
            data = null;
            pointer.close();
            pointer = null;
        }
    }

    /**
     * Swap the size of the matrix if needed
     */
    private void swapSize() {
        if (rows == columns) return;
        var tmp = rows;
        rows    = columns;
        columns = tmp;
    }
}
