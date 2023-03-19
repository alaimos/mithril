package com.alaimos.MITHrIL.FastCPUMath.FastCPUMatrix;

import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Reader.BinaryReader;
import com.alaimos.MITHrIL.api.Data.Writer.BinaryWriter;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CPUMatrixTest {

    protected static final double DELTA = 1e-6;
    protected static final double[] V1 = {4, 5, 6};
    protected static final double[] V2 = {6, 7, 8, 9};
    protected static final double[] M1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    protected static final double[] M2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    protected static final double[] M1_INV = {
            -6.388889e-01, -1.666667e-01, 3.055556e-01,
            -5.555556e-02, 5.504683e-17, 5.555556e-02,
            5.277778e-01, 1.666667e-01, -1.944444e-01
    };
    protected static final double[] M2_INV = {
            -0.37500000, -0.10000000, 0.17500000,
            -0.14583333, -0.03333333, 0.07916667,
            0.08333333, 0.03333333, -0.01666667,
            0.31250000, 0.10000000, -0.11250000
    };
    protected static final double[] M1_TIMES_M2 = {38, 44, 50, 56, 83, 98, 113, 128, 128, 152, 176, 200};
    protected static final double[] M1_TIMES_V1 = {32, 77, 122};
    protected static final double[] V2_TIMES_T_M2 = {80, 200, 320};
    protected static final double[] T_M2_TIMES_M1 = {84, 99, 114, 96, 114, 132, 108, 129, 150, 120, 144, 168};
    protected CPUMatrixFactory factory = new CPUMatrixFactory();
    protected CPUMatrix m1;
    protected CPUMatrix m2;

    @BeforeEach
    void setUp() {
        m1 = factory.of(M1, 3, 3);
        m2 = factory.of(M2, 3, 4);
    }

    @AfterEach
    public void tearDown() {
        m1.close();
        m2.close();
    }

    @Test
    @Order(1)
    void factoryTest() {
        try (var tm1 = factory.of(M1, 3, 3);
             var tm2 = factory.of(new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}})) {
            assertArrayEquals(M1, tm1.raw1D(), DELTA);
            assertArrayEquals(M1, tm2.raw1D(), DELTA);
        }
    }

    @Order(2)
    @Test
    void raw2D() {
        var expected = new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        var raw2D = m1.raw2D();
        assertEquals(expected.length, raw2D.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], raw2D[i], DELTA);
        }
    }

    @Order(3)
    @Test
    void raw1D() {
        assertArrayEquals(M1, m1.raw1D(), DELTA);
        assertArrayEquals(M2, m2.raw1D(), DELTA);
    }

    @Test
    void transpose() {
        var m3 = m2.transpose();
        assertEquals(4, m3.rows());
        assertEquals(3, m3.columns());
        assertArrayEquals(new double[]{1, 5, 9, 2, 6, 10, 3, 7, 11, 4, 8, 12}, m3.raw1D(), DELTA);
        m3.close();
    }

    @Test
    void transposeInPlace() {
        m2.transposeInPlace();
        assertArrayEquals(new double[]{1, 5, 9, 2, 6, 10, 3, 7, 11, 4, 8, 12}, m2.raw1D(), DELTA);
    }

    @Test
    void invert() {
        var m3 = m1.invert();
        assertEquals(3, m3.rows());
        assertEquals(3, m3.columns());
        assertArrayEquals(M1_INV, m3.raw1D(), DELTA);
        m3.close();
        m3 = m2.invert();
        assertEquals(4, m3.rows());
        assertEquals(3, m3.columns());
        assertArrayEquals(M2_INV, m3.raw1D(), DELTA);
        m3.close();
    }

    @Test
    void invertInPlace() {
        m1.invertInPlace();
        assertEquals(3, m1.rows());
        assertEquals(3, m1.columns());
        assertArrayEquals(M1_INV, m1.raw1D(), DELTA);
        m2.invertInPlace();
        assertEquals(4, m2.rows());
        assertEquals(3, m2.columns());
        assertArrayEquals(M2_INV, m2.raw1D(), DELTA);
    }

    @Test
    void preMultiply() {
        var tm2 = m2.transpose();
        var m3 = m1.preMultiply(tm2);
        var v3 = tm2.preMultiply(V2);
        assertEquals(4, m3.rows());
        assertEquals(3, m3.columns());
        assertArrayEquals(T_M2_TIMES_M1, m3.raw1D(), DELTA);
        assertEquals(3, v3.length);
        assertArrayEquals(V2_TIMES_T_M2, v3, DELTA);
        m3.close();
        tm2.close();
    }

    @Test
    void postMultiply() {
        var m3 = m1.postMultiply(m2);
        var v3 = m1.postMultiply(V1);
        assertEquals(3, m3.rows());
        assertEquals(4, m3.columns());
        assertArrayEquals(M1_TIMES_M2, m3.raw1D(), DELTA);
        assertEquals(3, v3.length);
        assertArrayEquals(M1_TIMES_V1, v3, DELTA);
        m3.close();
    }

    @Test
    void val() {
        assertEquals(1, m1.val(0, 0), DELTA);
        assertEquals(2, m1.val(0, 1), DELTA);
        assertEquals(3, m1.val(0, 2), DELTA);
        assertEquals(4, m1.val(1, 0), DELTA);
        assertEquals(5, m1.val(1, 1), DELTA);
        assertEquals(6, m1.val(1, 2), DELTA);
        assertEquals(7, m1.val(2, 0), DELTA);
        assertEquals(8, m1.val(2, 1), DELTA);
        assertEquals(9, m1.val(2, 2), DELTA);
        assertEquals(1, m2.val(0, 0), DELTA);
        assertEquals(2, m2.val(0, 1), DELTA);
        assertEquals(3, m2.val(0, 2), DELTA);
        assertEquals(4, m2.val(0, 3), DELTA);
        assertEquals(5, m2.val(1, 0), DELTA);
        assertEquals(6, m2.val(1, 1), DELTA);
        assertEquals(7, m2.val(1, 2), DELTA);
        assertEquals(8, m2.val(1, 3), DELTA);
        assertEquals(9, m2.val(2, 0), DELTA);
        assertEquals(10, m2.val(2, 1), DELTA);
        assertEquals(11, m2.val(2, 2), DELTA);
        assertEquals(12, m2.val(2, 3), DELTA);
    }

    @Test
    void row() {
        assertArrayEquals(new double[]{7, 8, 9}, m1.row(2), DELTA);
        assertArrayEquals(new double[]{1, 2, 3, 4}, m2.row(0), DELTA);
    }

    @Test
    void column() {
        assertArrayEquals(new double[]{3, 6, 9}, m1.column(2), DELTA);
        assertArrayEquals(new double[]{1, 5, 9}, m2.column(0), DELTA);
    }

    @Test
    void serializeTest() throws IOException {
        var file = "test.bin";
        var writer = new BinaryWriter<CPUMatrix>();
        writer.write(file, m1);
        assertTrue(new File(Utils.getAppDir(), file).exists());
        var reader = new BinaryReader<>(CPUMatrix.class);
        try (var m3 = reader.read(file)) {
            assertEquals(m1, m3);
        }
        new File(Utils.getAppDir(), file).delete();
    }
}