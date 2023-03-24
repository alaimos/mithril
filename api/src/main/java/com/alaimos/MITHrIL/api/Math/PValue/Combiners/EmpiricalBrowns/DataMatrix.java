package com.alaimos.MITHrIL.api.Math.PValue.Combiners.EmpiricalBrowns;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

/**
 * A data matrix with row names
 *
 * @param rowNames the row names
 * @param data     the data
 */
public record DataMatrix(String[] rowNames, double[][] data) {

    public DataMatrix {
        if (rowNames.length != data.length) throw new IllegalArgumentException("Number of rows do not match");
    }

    /**
     * Get the index of a row by its name
     *
     * @param row the row name
     * @return the index of the row
     */
    public int rowIndexByName(String row) {
        return ArrayUtils.indexOf(rowNames, row);
    }

    /**
     * Get the indexes of several rows by their names
     *
     * @param rows the row names
     * @return the indexes of the rows
     */
    public int @NotNull [] rowIndicesByNames(String @NotNull [] rows) {
        int[] indexes = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            indexes[i] = rowIndexByName(rows[i]);
        }
        return indexes;
    }

    /**
     * Get the number of rows
     *
     * @return the number of rows
     */
    public int rows() {
        return data.length;
    }

    /**
     * Get the number of columns
     *
     * @return the number of columns
     */
    public int cols() {
        return data[0].length;
    }

    /**
     * Get a row by its index
     *
     * @param row the row index
     * @return the row
     */
    public double[] getRow(int row) {
        if (row < 0 || row >= data.length) throw new OutOfRangeException(row, 0, data.length);
        return data[row].clone();
    }

    /**
     * Get a row by its name
     *
     * @param row the row name
     * @return the row
     */
    public double[] getRow(String row) {
        return getRow(rowIndexByName(row));
    }

    /**
     * Get multiple rows by their indexes
     *
     * @param rows the row indexes
     * @return the rows
     */
    public double @NotNull [] @NotNull [] getRows(int @NotNull [] rows) {
        var selection = new double[rows.length][];
        for (var i = 0; i < rows.length; i++) {
            selection[i] = getRow(rows[i]);
        }
        return selection;
    }

    /**
     * Get multiple rows by their names
     *
     * @param rows the row names
     * @return the rows
     */
    public double @NotNull [] @NotNull [] getRows(String[] rows) {
        return getRows(rowIndicesByNames(rows));
    }

    /**
     * Get a subset of this matrix
     *
     * @param indexes a set of row index
     * @return a sub-matrix
     */
    @Contract("_ -> new")
    public @NotNull DataMatrix subMatrix(int[] indexes) {
        var subData = getRows(indexes);
        var subRowNames = new String[indexes.length];
        for (var i = 0; i < subRowNames.length; i++) {
            subRowNames[i] = rowNames[indexes[i]];
        }
        return new DataMatrix(subRowNames, subData);
    }

    /**
     * Get a subset of this matrix
     *
     * @param rows a set of rows
     * @return a sub-matrix
     */
    @Contract("_ -> new")
    public @NotNull DataMatrix subMatrix(String[] rows) {
        return subMatrix(rowIndicesByNames(rows));
    }

    /**
     * Get the lower triangular part of this matrix
     *
     * @return the lower triangular matrix as an array
     */
    public double[] lowerTriangular() {
        return lowerTriangular(true);
    }

    /**
     * Get the lower triangular part of this matrix
     *
     * @param keepDiag keeps the diagonal of the matrix?
     * @return the lower triangular matrix as an array
     */
    public double[] lowerTriangular(boolean keepDiag) {
        var lower = new ArrayList<Double>();
        for (var i = 0; i < data.length; i++) {
            for (var j = 0; (keepDiag) ? j <= i : j < i; j++) {
                lower.add(data[i][j]);
            }
        }
        return lower.stream().mapToDouble(v -> v).toArray();
    }

    /**
     * Compute the covariance matrix of this matrix
     *
     * @return the covariance matrix
     */
    @Contract(" -> new")
    public @NotNull DataMatrix computeCovarianceMatrix() {
        var mx = MatrixUtils.createRealMatrix(this.data);
        mx = mx.transpose();
        var cov = new Covariance(mx).getCovarianceMatrix();
        return new DataMatrix(this.rowNames.clone(), cov.transpose().getData());
    }

    /**
     * Apply a transformation to this matrix
     *
     * @param f a transformation
     */
    public void transform(Function<double[], double[]> f) {
        for (int i = 0; i < data.length; i++) {
            data[i] = f.apply(data[i]);
        }
    }

    /**
     * Convert this matrix to a string
     *
     * @return the string representation of this matrix
     */
    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rowNames.length; i++) {
            sb.append(rowNames[i])
              .append(Arrays.toString(data[i]).replace('[', '\t').replace(']', '\n').replace(',', '\t'));
        }
        return sb.toString();
    }
}
