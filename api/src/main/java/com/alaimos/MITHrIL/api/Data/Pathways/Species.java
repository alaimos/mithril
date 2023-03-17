package com.alaimos.MITHrIL.api.Data.Pathways;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.MiRNATargetsReader;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.PathwayRepositoryReader;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.ReactomeRepositoryReader;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.TranscriptionFactorsReader;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.MiRNAContainer;

import java.io.IOException;

public record Species(String id, String name, boolean hasMiRNA, boolean hasTranscriptionFactors, boolean hasReactome,
                      String pathwayUrl, String miRNAUrl, String transcriptionFactorsUrl, String reactomeUrl) {

    /**
     * Returns the repository of pathways for this species
     *
     * @return a repository of pathways
     * @throws IOException if an error occurs while reading the repository
     */
    public Repository repository() throws IOException {
        return new PathwayRepositoryReader(pathwayUrl).read();
    }

    /**
     * Adds the Reactome pathways to the repository
     *
     * @param repository a repository of pathways
     * @return a repository of pathways
     * @throws IOException if an error occurs while reading the repository
     */
    public Repository addReactomeToRepository(Repository repository) throws IOException {
        if (!hasReactome) return repository;
        return new ReactomeRepositoryReader(reactomeUrl, repository).read();
    }

    /**
     * Returns the miRNA container for this species
     *
     * @return a miRNA container
     * @throws IOException if an error occurs while reading the container
     */
    public MiRNAContainer miRNAContainer() throws IOException {
        if (!hasMiRNA) return new MiRNAContainer();
        var container = new MiRNATargetsReader(miRNAUrl).read();
        if (!hasTranscriptionFactors) return container;
        return new TranscriptionFactorsReader(transcriptionFactorsUrl, container).read();
    }

}
