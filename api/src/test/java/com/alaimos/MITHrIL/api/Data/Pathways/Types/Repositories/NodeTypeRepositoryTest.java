package com.alaimos.MITHrIL.api.Data.Pathways.Types.Repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTypeRepositoryTest {

    @DisplayName("Test Singleton NodeTypeRepository")
    @Test
    void getInstance() {
        var repository = NodeTypeRepository.getInstance();
        var repository1 = NodeTypeRepository.getInstance();
        assertEquals(repository, repository1);
    }

    @DisplayName("Test fromString()")
    @Test
    void fromString() {
        var repository = NodeTypeRepository.getInstance();
        assertEquals(repository.valueOf("GENE"), repository.fromString("gene"));
        assertEquals(repository.valueOf("OTHER"), repository.fromString("test"));
        assertEquals(repository.valueOf("MIRNA"), repository.fromString("miRNA"));
    }

    @DisplayName("Test add()")
    @Test
    void add() {
        var repository = NodeTypeRepository.getInstance();
        var testType = repository.add("TEST_TYPE", "-1");
        assertEquals(-1, testType.sign(), 0.001);
        assertEquals(repository.valueOf("TEST_TYPE"), testType);
    }

}