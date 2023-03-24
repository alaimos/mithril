package com.alaimos.MITHrIL.api.Data.Reader;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteVersionReaderTest {

    @Test
    void read() throws IOException {
        var reader = new RemoteVersionReader();
        var version = reader.read();
        assertNotNull(version);
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(\\.\\d+)?(-SNAPSHOT)?"));
    }
}