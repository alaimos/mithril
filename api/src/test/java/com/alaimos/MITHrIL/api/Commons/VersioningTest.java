package com.alaimos.MITHrIL.api.Commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersioningTest {

    @Test
    void getCurrentVersion() {
        var versioning = new Versioning();
        var version = versioning.getCurrentVersion();
        assertNotNull(version);
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+(\\.\\d+)?(-SNAPSHOT)?"));
    }
}