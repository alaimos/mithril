package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Reader.DynamicTextFileReader;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MatrixBuilderFromMetapathway;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MetapathwayBuilderFromOptions;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM;
import com.alaimos.MITHrIL.app.CommandLine.Options.PHENSIMOptions;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PHENSIMInputReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.EdgeSubtypeReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.EdgeTypeReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.NodeTypeReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.PathwayExtensionReader;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Data.Writers.PHENSIM.ActivityScoreMatrixWriter;
import com.alaimos.MITHrIL.app.Data.Writers.PHENSIM.ExtendedSIFWriter;
import com.alaimos.MITHrIL.app.Data.Writers.PHENSIM.SBMLWriter;
import com.alaimos.MITHrIL.app.Data.Writers.PHENSIM.SimulationOutputWriter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class PHENSIMService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(PHENSIMService.class);

    protected PHENSIMOptions options = new PHENSIMOptions();

    @Override
    public String getShortName() {
        return "phensim";
    }

    @Override
    public String getDescription() {
        return "runs the PHENSIM algorithm";
    }

    @Override
    public OptionsInterface getOptions() {
        return options;
    }

    @Override
    public void run() {
        try {
            if (options.verbose) {
                Configurator.setRootLevel(Level.INFO);
            } else {
                Configurator.setRootLevel(Level.WARN);
            }
            checkInputParameters();
            var random = random();
            var extManager = ExtensionManager.INSTANCE;
            log.info("Reading input file");
            var input = readInputFile();
            var metapathwayRepository = MetapathwayBuilderFromOptions.build(options, random);
            var extensionGraph = prepareExtensionGraph();
            if (extensionGraph != null) {
                metapathwayRepository.extendWith(extensionGraph);
            }
            var nodesToRemove = readNodesToRemove();
            for (var node : nodesToRemove) {
                metapathwayRepository.removeNode(node);
            }
            var inversionMatrixFactory = matrixFactory(options.inversionFactory);
            RepositoryMatrix metapathwayMatrix;
            if (options.customizePathwayMatrix) {
                metapathwayMatrix = MatrixBuilderFromMetapathway.build(
                        metapathwayRepository,
                        inversionMatrixFactory,
                        extractCustomizationNodesFromInput(input, metapathwayRepository)
                );
            } else {
                metapathwayMatrix = MatrixBuilderFromMetapathway.build(metapathwayRepository, inversionMatrixFactory);
            }
            var multiplicationMatrixFactory = matrixFactory(options.multiplicationFactory);
            log.info("Running PHENSIM");
            try (var phensim = new PHENSIM()) {
                phensim.constraints(input)
                       .nonExpressedNodes(readNonExpressedNodes())
                       .repository(metapathwayRepository)
                       .repositoryMatrix(metapathwayMatrix)
                       .matrixFactory(multiplicationMatrixFactory)
                       .random(random)
                       .batchSize(options.batchSize)
                       .numberOfRepetitions(options.iterations)
                       .numberOfSimulations(options.simulations)
                       .threads(options.threads)
                       .pValueAdjuster(extManager.getExtension(AdjusterInterface.class, options.pValueAdjuster))
                       .epsilon(options.epsilon)
                       .run();
                var output = phensim.output(appendAllRunsToOutput());
                log.info("Writing output file");
                new SimulationOutputWriter(metapathwayRepository, metapathwayMatrix, input).write(
                        options.output, output);
                if (options.outputPathwayMatrix != null) {
                    log.info("Writing pathway activity scores matrix");
                    new ActivityScoreMatrixWriter(metapathwayMatrix, true).write(options.outputPathwayMatrix, output);
                }
                if (options.outputNodesMatrix != null) {
                    log.info("Writing node activity scores matrix");
                    new ActivityScoreMatrixWriter(metapathwayMatrix, false).write(options.outputNodesMatrix, output);
                }
                if (options.outputSBML != null) {
                    log.info("Writing SBML output");
                    new SBMLWriter(metapathwayRepository, metapathwayMatrix).write(options.outputSBML, output);
                }
                if (options.outputSIF != null) {
                    log.info("Writing Extended SIF output");
                    new ExtendedSIFWriter(metapathwayRepository, metapathwayMatrix).write(options.outputSIF, output);
                }
            }
            log.info("Done");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error("An error occurred", e);
        }
    }

    private void validateOutputDirectory(File f, String name) {
        if (f == null) return;
        var parent = f.getParentFile();
        if (parent == null) parent = new File(".");
        if (!parent.exists()) {
            throw new IllegalArgumentException(name + " directory does not exist");
        }
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException(name + " is not a directory");
        }
        if (!parent.canWrite()) {
            throw new IllegalArgumentException("Cannot write to " + name + " directory");
        }
    }

    private void checkInputParameters() {
        if (options.input == null) {
            throw new IllegalArgumentException("Input file is not specified");
        }
        if (!options.input.exists() || !options.input.canRead()) {
            throw new IllegalArgumentException("Input file does not exist or is not a file");
        }
        validateOutputDirectory(options.output, "Output");
        validateOutputDirectory(options.outputPathwayMatrix, "Pathway matrix output");
        validateOutputDirectory(options.outputNodesMatrix, "Nodes matrix output");
        validateOutputDirectory(options.outputSBML, "SBML output");
        validateOutputDirectory(options.outputSIF, "SIF output");
    }

    private ExpressionConstraint[] readInputFile() throws IOException {
        return new PHENSIMInputReader(false).read(options.input);
    }

    private String @NotNull [] readNonExpressedNodes() throws IOException {
        if (options.nonExpressedFile == null) return new String[0];
        var nodesList = new DynamicTextFileReader().read(options.nonExpressedFile);
        return nodesList.toArray(new String[0]);
    }

    private String @NotNull [] readNodesToRemove() throws IOException {
        if (options.removeNodesFile == null) return new String[0];
        var nodesList = new DynamicTextFileReader().read(options.removeNodesFile);
        return nodesList.toArray(new String[0]);
    }

    private @Nullable Graph prepareExtensionGraph() throws IOException {
        var extensionGraphFile = options.metapathwayExtensionInputFile;
        if (extensionGraphFile == null || !extensionGraphFile.exists() || !extensionGraphFile.canRead()) return null;
        if (options.customNodeTypeInputFile != null && options.customNodeTypeInputFile.exists()) {
            new NodeTypeReader().read(options.customNodeTypeInputFile);
        }
        if (options.customEdgeTypeInputFile != null && options.customEdgeTypeInputFile.exists()) {
            new EdgeTypeReader().read(options.customEdgeTypeInputFile);
        }
        if (options.customEdgeSubtypeInputFile != null && options.customEdgeSubtypeInputFile.exists()) {
            new EdgeSubtypeReader().read(options.customEdgeSubtypeInputFile);
        }
        return new PathwayExtensionReader().read(extensionGraphFile);
    }

    private @NotNull Random random() {
        return options.randomSeed == null ? new Random() : new Random(options.randomSeed);
    }

    private boolean appendAllRunsToOutput() {
        return options.outputNodesMatrix != null || options.outputPathwayMatrix != null;
    }

    private @NotNull MatrixFactoryInterface<?> matrixFactory(String name) {
        var extManager = ExtensionManager.INSTANCE;
        var factory = extManager.getExtension(MatrixFactoryInterface.class, name);
        if (factory == null) {
            throw new IllegalArgumentException("Matrix factory not found");
        }
        if (options.threads > 0) {
            factory.setMaxThreads(options.threads);
        }
        return factory;
    }

    private @NotNull List<String> extractCustomizationNodesFromInput(
            @NotNull ExpressionConstraint @NotNull [] input,
            @NotNull Repository repository
    ) {
        var output = new ObjectArrayList<String>();
        var metapathway = repository.get().graph();
        for (var constraint : input) {
            var nodeId = constraint.nodeId();
            if (!metapathway.hasNode(nodeId)) continue;
            output.add(nodeId);
        }
        output.sort((o1, o2) -> {
            var n1Degree = metapathway.outDegree(metapathway.node(o1));
            var n2Degree = metapathway.outDegree(metapathway.node(o2));
            return Integer.compare(n2Degree, n1Degree);
        });
        return output;
    }
}
