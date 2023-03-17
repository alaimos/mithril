package com.alaimos.MITHrIL.api.Data;

import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Reader.BinaryReader;
import com.alaimos.MITHrIL.api.Data.Writer.BinaryWriter;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BinaryReaderAndWriterTest {

    public static final String FILE = "test.bin";
    public static final HashMap<String, Double> MAP = new HashMap<>(Map.of("key1", 0.1d, "key2", 1d / 3d));

    @Order(1)
    @DisplayName("Test BinaryWriter")
    @Test
    public void testBinaryWriter() throws IOException {
        var writer = new BinaryWriter<HashMap<String, Double>>();
        writer.write(FILE, MAP);
        assertTrue(new File(Utils.getAppDir(), FILE).exists());
    }

    @Order(2)
    @DisplayName("Test BinaryReader")
    @Test
    public void testBinaryReader() throws IOException, ClassNotFoundException {
        var reader = new BinaryReader<>(HashMap.class);
        var map = reader.read(FILE);
        assertEquals(MAP, map);
    }

    @AfterAll
    public static void tearDown() {
        new File(Utils.getAppDir(), FILE).delete();
    }

}