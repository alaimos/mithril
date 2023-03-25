package com.alaimos.MITHrIL.app.Algorithms;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.EdgeWeightComputationInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.NodeWeightComputationInterface;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.SpeciesDatabaseReader;
import com.alaimos.MITHrIL.api.Data.Reader.TextFileReader;
import com.alaimos.MITHrIL.app.CommandLine.Options.MetapathwayOptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Random;

public class MetapathwayBuilder implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MetapathwayBuilder.class);
    private final MetapathwayOptions options;
    private final Random random;
    private Repository metapathway = null;


    public MetapathwayBuilder(MetapathwayOptions options, Random random) {
        this.options = options;
        this.random = random;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            log.info("Reading species database");
            var org = options.organism;
            var species = SpeciesDatabaseReader.INSTANCE.read();
            if (!species.containsKey(org)) {
                log.error("Invalid species: {} not found.", org);
                return;
            }
            var s = species.get(org);
            log.info("Reading pathways for {} into the repository", s.name());
            var repository = s.repository();
            if (options.reactome && s.hasReactome()) {
                log.info("Adding Reactome pathways to the repository");
                s.addReactomeToRepository(repository);
            }
            log.debug("The repository contains {} pathways", repository.size());
            if (options.decoys) {
                repository.addDecoys(random, false);
                log.debug("The repository now contains {} pathways", repository.size());
            }
            log.info("Building metapathway");
            metapathway = s.repository().buildMetapathway(prepareFilter(), false, false);
            if (!options.noExtension && s.hasMiRNA()) {
                log.info("Reading miRNA for {} with minimum evidence {}", s.name(), options.extensionEvidenceType.name());
                var containerGraph = s.miRNAContainer().toGraph(options.extensionEvidenceType);
                log.info("Extending metapathway with miRNAs");
                metapathway.extendWith(containerGraph);
            }
            log.info("Setting edge weight computation method to {}", options.edgeWeightComputationMethod);
            Edge.setWeightComputationMethod(ExtensionManager.INSTANCE.getExtension(EdgeWeightComputationInterface.class, options.edgeWeightComputationMethod));
            log.info("Setting node weight computation method to {}", options.nodeWeightComputationMethod);
            Node.setWeightComputationMethod(ExtensionManager.INSTANCE.getExtension(NodeWeightComputationInterface.class, options.nodeWeightComputationMethod));
            log.info("Repository ready");
        } catch (Throwable e) {
            log.error("An error occurred while building the metapathway", e);
        }
    }

    /**
     * Returns the metapathway
     *
     * @return the metapathway
     */
    public Repository get() {
        if (metapathway == null) run();
        return metapathway;
    }

    private List<String> readFile(File f) {
        if (f == null) return null;
        if (!f.exists()) return null;
        if (!f.canRead()) return null;
        try {
            return new TextFileReader().read(f);
        } catch (Throwable e) {
            log.error("An error occurred while reading file {}", f.getAbsolutePath(), e);
            return null;
        }
    }

    private Repository.@NotNull RepositoryFilter prepareFilter() {
        var includePathways = readFile(options.includePathways);
        var excludePathways = readFile(options.excludePathways);
        var includeCategories = options.includeCategories != null && options.includeCategories.length > 0 ?
                List.of(options.includeCategories) : null;
        var excludeCategories = options.excludeCategories != null && options.excludeCategories.length > 0 ?
                List.of(options.excludeCategories) : null;
        return new Repository.RepositoryFilter(includeCategories, excludeCategories, includePathways, excludePathways, null);
    }
}
