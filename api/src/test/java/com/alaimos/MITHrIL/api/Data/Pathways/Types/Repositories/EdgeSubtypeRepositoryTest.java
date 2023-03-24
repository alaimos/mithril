package com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeSubtypeRepositoryTest {

    @DisplayName("Test Singleton EdgeSubtypeRepository")
    @Test
    void getInstance() {
        var repository = EdgeSubtypeRepository.getInstance();
        var repository1 = EdgeSubtypeRepository.getInstance();
        assertEquals(repository, repository1);
    }

    @DisplayName("Test fromString()")
    @Test
    void fromString() {
        var repository = EdgeSubtypeRepository.getInstance();
        assertEquals(repository.valueOf("COMPOUND"), repository.fromString("compound"));
        assertEquals(repository.valueOf("MISSING_INTERACTION"), repository.fromString("MISSING INTERACTION"));
        assertEquals(repository.valueOf("TFMIRNA_ACTIVATION"), repository.fromString("TfMirna Activation"));
    }

    @DisplayName("Test add()")
    @Test
    void add() {
        var repository = EdgeSubtypeRepository.getInstance();
        var testType = repository.add("TEST_TYPE", "1.0", "1", "-T->");
        assertEquals(1.0, testType.weight(), 0.0001);
        assertEquals(1, testType.priority());
        assertEquals("-T->", testType.symbol());
        assertEquals(repository.valueOf("TEST_TYPE"), testType);
    }
}