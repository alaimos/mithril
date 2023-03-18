package com.alaimos.MITHrIL.FastCPUMath.FastCPUMatrix;

import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.pytorch.Tensor;
import org.bytedeco.pytorch.global.torch;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

import static org.bytedeco.pytorch.global.torch.dtype;
import static org.bytedeco.pytorch.global.torch.zeros;

public class CPUMatrix implements MatrixInterface<CPUMatrix> {

    @Serial
    private static final long serialVersionUID = -8761988017888622755L;

    private transient Tensor internalMatrix;

    private transient DoublePointer pointer = null;

    private transient double[] data = null;

    private int rows;

    private int columns;

    private CPUMatrix(Tensor matrix, int rows, int columns) {
        internalMatrix = matrix;
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
        internalMatrix = tensorFromArray(matrix, new long[]{rows, columns});
        this.rows = rows;
        this.columns = columns;
    }

    public CPUMatrix(@NotNull MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            internalMatrix = dm.internalMatrix;
            rows = dm.rows;
            columns = dm.columns;
        } else {
            internalMatrix = tensorFromArray(matrix.raw1D(), new long[]{matrix.rows(), matrix.columns()});
            rows = matrix.rows();
            columns = matrix.columns();
        }
    }

    private void readData() {
        if (pointer == null) {
            pointer = internalMatrix.data_ptr_double();
            data = new double[rows * columns];
            pointer.get(data);
        }
    }

    /**
     * Transpose the matrix
     *
     * @return a new matrix
     */
    @Override
    public CPUMatrix transpose() {
        return new CPUMatrix(internalMatrix.t(), columns, rows);
    }

    /**
     * Transpose the matrix in place
     */
    @Override
    public void transposeInPlace() {
        if (pointer != null) {
            data = null;
            pointer.close();
            pointer = null;
        }
        var oldInternal = internalMatrix;
        var tmp = rows;
        internalMatrix = internalMatrix.t();
        oldInternal.close();
        rows = columns;
        columns = tmp;
    }

    /**
     * Invert the matrix.
     * It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     *
     * @return a new matrix
     */
    @Override
    public CPUMatrix invert() {
        return new CPUMatrix(internalMatrix.pinverse(), rows, columns);
    }

    /**
     * Invert the matrix in place.
     * It uses the Moore-Penrose pseudo-inverse to invert the matrix.
     */
    @Override
    public void invertInPlace() {
        if (pointer != null) {
            data = null;
            pointer.close();
            pointer = null;
        }
        var oldInternal = internalMatrix;
        internalMatrix = internalMatrix.pinverse();
        oldInternal.close();
    }

    /**
     * Pre-multiply this matrix by another matrix.
     * That is, the operation is performed as matrix * this.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public CPUMatrix preMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            var rows = dm.rows();
            return new CPUMatrix(dm.internalMatrix.matmul(internalMatrix), rows, columns);
        } else {
            return new CPUMatrix(matrix).preMultiply(this);
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
        try (var vectorTensor = tensorFromArray(vector, new long[]{1, vector.length});
             var result = vectorTensor.matmul(internalMatrix)) {
            return tensorToArray(result);
        }
    }

    /**
     * Post-multiply this matrix by another matrix.
     * That is, the operation is performed as this * matrix.
     *
     * @param matrix the other matrix
     * @return a new matrix
     */
    @Override
    public CPUMatrix postMultiply(MatrixInterface<?> matrix) {
        if (matrix instanceof CPUMatrix dm) {
            var columns = dm.columns();
            return new CPUMatrix(internalMatrix.matmul(dm.internalMatrix), rows, columns);
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
        try (var vectorTensor = tensorFromArray(vector, new long[]{vector.length, 1});
             var result = internalMatrix.matmul(vectorTensor)) {
            return tensorToArray(result);
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
        readData();
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
        readData();
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
        readData();
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
    public double[] raw1D() {
        readData();
        return data;
    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.raw1D());
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        double[] raw = (double[]) ois.readObject();
        internalMatrix = zeros(new long[]{rows, columns}, dtype(torch.ScalarType.Double));
        try (var ptr = internalMatrix.data_ptr_double()) {
            ptr.put(raw);
        }
    }

    /**
     * Releases the tensor and its backing memory.
     */
    @Override
    public void close() {
        if (pointer != null) {
            data = null;
            pointer.close();
            pointer = null;
        }
        internalMatrix.close();
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

}
