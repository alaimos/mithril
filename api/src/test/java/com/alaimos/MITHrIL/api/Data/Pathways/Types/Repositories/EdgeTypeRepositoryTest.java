package com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeTypeRepositoryTest {

    @DisplayName("Test Singleton EdgeTypeRepository")
    @Test
    void getInstance() {
        var repository = EdgeTypeRepository.getInstance();
        var repository1 = EdgeTypeRepository.getInstance();
        assertEquals(repository, repository1);
    }

    @DisplayName("Test fromString()")
    @Test
    void fromString() {
        var repository = EdgeTypeRepository.getInstance();
        assertEquals(repository.valueOf("ECREL"), repository.fromString("ecrel"));
        assertEquals(repository.valueOf("PPREL"), repository.fromString("pprel"));
        assertEquals(repository.valueOf("MGREL"), repository.fromString("mGrEl"));
    }

    @DisplayName("Test add()")
    @Test
    void add() {
        var repository = EdgeTypeRepository.getInstance();
        var testType = repository.add("TEST_TYPE");
        assertEquals(repository.valueOf("TEST_TYPE"), testType);
    }
}