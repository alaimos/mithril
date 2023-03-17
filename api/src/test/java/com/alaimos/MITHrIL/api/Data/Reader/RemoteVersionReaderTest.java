package com.alaimos.MITHrIL.api.Data.Reader;

import com.alaimos.MITHrIL.api.Commons.Constants;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteVersionReaderTest {

    @Test
    void read() throws IOException {
        var reader = new RemoteVersionReader();
        var version = reader.read(Constants.INSTANCE.get("mithril_version_file"));
        assertNotNull(version);
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(\\.\\d+)?(-SNAPSHOT)?"));
    }
}