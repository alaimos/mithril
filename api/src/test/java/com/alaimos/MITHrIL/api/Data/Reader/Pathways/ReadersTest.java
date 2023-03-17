package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ReadersTest {

    @DisplayName("Test SpeciesDatabaseReader")
    @Test
    public void testSpeciesDatabaseReader() throws IOException {
        var species = SpeciesDatabaseReader.INSTANCE.read();
        assertNotNull(species);
        assertTrue(species.size() > 0);
        assertTrue(species.containsKey("hsa"));
        assertTrue(species.get("hsa").hasReactome());
        assertTrue(species.get("hsa").hasMiRNA());
        assertTrue(species.get("hsa").hasTranscriptionFactors());
    }

    @DisplayName("Test PathwayRepositoryReader")
    @Test
    public void testPathwayRepositoryReader() throws IOException {
        var species = SpeciesDatabaseReader.INSTANCE.read();
        var repository = species.get("hsa").repository();
        assertNotNull(repository);
        assertTrue(repository.size() > 0);
        assertNotNull(repository.get("path:hsa00010"));
        var graph = repository.get("path:hsa00010").graph();
        assertNotNull(graph);
        assertTrue(graph.countNodes() > 0);
        assertTrue(graph.countEdges() > 0);
    }

    @DisplayName("Test ReactomeRepositoryReader")
    @Test
    public void testReactomeRepositoryReader() throws IOException {
        var species = SpeciesDatabaseReader.INSTANCE.read();
        var repository = species.get("hsa").repository();
        var size = repository.size();
        var reactome = species.get("hsa").addReactomeToRepository(repository);
        assertSame(repository, reactome);
        assertTrue(reactome.size() > size);
        assertNotNull(repository.get("path:hsa00010"));
        var graph = repository.get("path:hsa00010").graph();
        assertNotNull(graph);
        assertTrue(graph.countNodes() > 0);
        assertTrue(graph.countEdges() > 0);
        assertNotNull(repository.get("R-HSA-6803157"));
        graph = repository.get("R-HSA-6803157").graph();
        assertNotNull(graph);
        assertTrue(graph.countNodes() > 0);
        assertTrue(graph.countEdges() > 0);
    }

    @DisplayName("Test MiRNA Container and Reader")
    @Test
    public void testMiRNAReaders() throws IOException {
        var species = SpeciesDatabaseReader.INSTANCE.read();
        var mirnas = species.get("hsa").miRNAContainer();
        assertNotNull(mirnas);
        assertTrue(mirnas.size() > 0);
        assertTrue(mirnas.containsKey("hsa-miR-99b-3p"));
        var mirna = mirnas.get("hsa-miR-99b-3p");
        assertNotNull(mirna);
        assertNotNull(mirna.id());
        assertNotNull(mirna.targets());
        assertTrue(mirna.targets().size() > 0);
        var target = mirna.targets().iterator().next();
        assertNotNull(target);
        assertNotNull(target.id());
        assertNotNull(mirna.transcriptionFactors());
        assertTrue(mirna.transcriptionFactors().size() > 0);
        var tf = mirna.transcriptionFactors().iterator().next();
        assertNotNull(tf);
        assertNotNull(tf.id());
    }

}