package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Reader.DynamicTextFileReader;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MatrixBuilderFromMetapathway;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MetapathwayBuilderFromOptions;
import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.CoverRanking;
import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.FastPHENSIM;
import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.SetBuilder;
import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.SetCoveringAlgorithm;
import com.alaimos.MITHrIL.app.CommandLine.Options.ReversePHENSIMOptions;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PHENSIMInputReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.EdgeSubtypeReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.EdgeTypeReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.NodeTypeReader;
import com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension.PathwayExtensionReader;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReversePHENSIMService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(ReversePHENSIMService.class);

    protected ReversePHENSIMOptions options = new ReversePHENSIMOptions();

    @Override
    public String getShortName() {
        return "reverse-phensim";
    }

    @Override
    public String getDescription() {
        return "runs the reverse PHENSIM algorithm";
    }

    @Override
    public OptionsInterface getOptions() {
        return options;
    }

    @Override
    public void run() {
        try {
            if (options.verbose) {
                Configurator.setLevel(Logger.ROOT_LOGGER_NAME, Level.INFO);
            } else {
                Configurator.setLevel(Logger.ROOT_LOGGER_NAME, Level.WARN);
            }
            checkInputParameters();
            var random = random();
            var extManager = ExtensionManager.INSTANCE;
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
            var metapathwayMatrix = MatrixBuilderFromMetapathway.build(metapathwayRepository, inversionMatrixFactory);
            log.info("Creating reversed metapathway matrix");
            var invertedMetapathwayRepository = metapathwayRepository.inverted();
            var invertedMetapathwayMatrix = MatrixBuilderFromMetapathway.build(
                    invertedMetapathwayRepository, inversionMatrixFactory
            );
            var multiplicationMatrixFactory = matrixFactory(options.multiplicationFactory);
            log.info("Reading input files");
            var input = readInputFile();
            var nonExpressedNodes = readNonExpressedNodes();
            var targetNodes = findTargetNodes(metapathwayRepository, input);
            log.debug(
                    "Input nodes: {} - Non-expressed nodes: {} - Target nodes: {}", input.length,
                    nonExpressedNodes.length, targetNodes.length
            );
            log.info("Running reverse PHENSIM");
            try (var phensim = new FastPHENSIM()) {
                phensim.constraints(input)
                       .nonExpressedNodes(nonExpressedNodes)
                       .repository(invertedMetapathwayRepository)
                       .repositoryMatrix(invertedMetapathwayMatrix)
                       .matrixFactory(multiplicationMatrixFactory)
                       .random(random)
                       .batchSize(options.batchSize)
                       .numberOfRepetitions(options.iterations)
                       .numberOfSimulations(options.simulations)
                       .threads(options.threads)
                       .epsilon(options.epsilon)
                       .run();
                var reversePhensimOutput = phensim.output();
                log.info("Filtering target nodes");
                targetNodes = filterTargetNodes(targetNodes, invertedMetapathwayMatrix, reversePhensimOutput);
                log.debug("Target nodes: {}", targetNodes.length);
                log.info("Computing coverages");
                var builder = new SetBuilder();
                builder.nodesToCover(input)
                       .nonExpressedNodes(nonExpressedNodes)
                       .forwardRepository(metapathwayRepository)
                       .forwardRepositoryMatrix(metapathwayMatrix)
                       .reverseRepositoryMatrix(invertedMetapathwayMatrix)
                       .random(random)
                       .batchSize(options.batchSize)
                       .reverseOutput(reversePhensimOutput)
                       .targetNodes(targetNodes)
                       .threads(options.threads)
                       .epsilon(options.epsilon)
                       .matrixFactory(multiplicationMatrixFactory)
                       .run();
                log.info("Computing set covering");
                var subsetsAndMap = builder.output();
                var subsetMap = subsetsAndMap.left();
                var subsets = subsetsAndMap.right();
                var id2Index = metapathwayMatrix.pathwayMatrix().id2Index();
                System.out.println(subsets.size() + " subsets");
                var universe = new IntOpenHashSet(Arrays.stream(input)
                                                        .mapToInt(constraint -> {
                                                            var nodeIdx = id2Index.getInt(constraint.nodeId());
                                                            var direction = constraint.direction();
                                                            return direction == RandomExpressionGenerator.ExpressionDirection.OVEREXPRESSION
                                                                    ? nodeIdx
                                                                    : -nodeIdx;
                                                        })
                                                        .toArray());
                var solutions = SetCoveringAlgorithm.of(universe, subsets).run();
                log.info("Ranking solutions");
                var constraintsSet = convertCoveringSetsToExpressionConstraints(solutions, subsetMap);
                var rankingAlgorithm = new CoverRanking();
                rankingAlgorithm.reverseConstraints(input)
                                .nonExpressedNodes(nonExpressedNodes)
                                .repository(metapathwayRepository)
                                .forwardRepositoryMatrix(metapathwayMatrix)
                                .random(random)
                                .batchSize(options.batchSize)
                                .threads(options.threads)
                                .epsilon(options.epsilon)
                                .coveringSets(constraintsSet)
                                .matrixFactory(multiplicationMatrixFactory)
                                .run();
                var ranking = rankingAlgorithm.output();
                for (var r : ranking) {
                    log.info("Nodes: {} - Coverage: {}", Arrays.toString(r.covering()), r.coverage());
                }


//                log.info("Writing output file");
//                new SimulationOutputWriter(metapathwayRepository, metapathwayMatrix, input).write(
//                        options.output, output);
//                if (options.outputPathwayMatrix != null) {
//                    log.info("Writing pathway activity scores matrix");
//                    new ActivityScoreMatrixWriter(metapathwayMatrix, true).write(options.outputPathwayMatrix, output);
//                }
//                if (options.outputNodesMatrix != null) {
//                    log.info("Writing node activity scores matrix");
//                    new ActivityScoreMatrixWriter(metapathwayMatrix, false).write(options.outputNodesMatrix, output);
//                }
//                if (options.outputSBML != null) {
//                    log.info("Writing SBML output");
//                    new SBMLWriter(metapathwayRepository, metapathwayMatrix).write(options.outputSBML, output);
//                }
//                if (options.outputSIF != null) {
//                    log.info("Writing Extended SIF output");
//                    new ExtendedSIFWriter(metapathwayRepository, metapathwayMatrix).write(options.outputSIF, output);
//                }
            }
            log.info("Done");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error("An error occurred", e);
        }
    }

    private void validateOutputDirectory(File f) {
        if (f == null) return;
        var parent = f.getParentFile();
        if (parent == null) parent = new File(".");
        if (!parent.exists()) {
            throw new IllegalArgumentException("Output directory does not exist");
        }
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Output is not a directory");
        }
        if (!parent.canWrite()) {
            throw new IllegalArgumentException("Cannot write to output directory");
        }
    }

    private void checkInputParameters() {
        if (options.input == null) {
            throw new IllegalArgumentException("Input file is not specified");
        }
        if (!options.input.exists() || !options.input.canRead()) {
            throw new IllegalArgumentException("Input file does not exist or is not a file");
        }
        validateOutputDirectory(options.output);
    }

    private ExpressionConstraint[] readInputFile() throws IOException {
        return new PHENSIMInputReader(false).read(options.input);
    }

    private String @NotNull [] findTargetNodes(Repository repository, ExpressionConstraint[] input) throws IOException {
        var targetNodes = readTargetNodes();
        if (targetNodes != null) return targetNodes;
        var graph = repository.get().graph();
        var minLevel = options.minimumLevel;
        var maxLevel = options.maximumLevel;
        var targetSet = new HashSet<String>();
        for (var constraint : input) {
            var queue = new ArrayDeque<IntObjectPair<String>>();
            var visited = new HashSet<String>();
            queue.add(IntObjectPair.of(0, constraint.nodeId()));
            while (!queue.isEmpty()) {
                var pair = queue.remove();
                var level = pair.firstInt();
                var node = pair.second();
                if (level >= minLevel && level <= maxLevel) {
                    targetSet.add(node);
                }
                if (level >= maxLevel) continue;
                if (visited.contains(node)) continue;
                visited.add(node);
                var neighbors = graph.ingoingEdges(graph.node(node));
                for (var neighbor : neighbors) {
                    queue.add(IntObjectPair.of(level + 1, neighbor.source().id()));
                }
            }
        }
        return targetSet.toArray(new String[0]);
    }

    private String[] filterTargetNodes(
            String[] targetNodes, RepositoryMatrix matrix, FastPHENSIM.SimulationOutput output
    ) {
        if (options.targetListFile == null) return targetNodes;
        var id2Index = matrix.pathwayMatrix().id2Index();
        var activityScores = output.nodeActivityScores();
        var pValues = output.nodePValues();
        var pValueThreshold = options.maxPFilter;
        var filtered = new HashSet<String>();
        for (var node : targetNodes) {
            var index = id2Index.getInt(node);
            if (activityScores[index] != 0.0 && pValues[index] <= pValueThreshold) {
                filtered.add(node);
            }
        }
        return filtered.toArray(new String[0]);
    }

    private @NotNull Collection<ExpressionConstraint[]> convertCoveringSetsToExpressionConstraints(
            @NotNull Collection<IntSet> coveringSets, List<ExpressionConstraint[]> subsetMap
    ) {
        var result = new HashSet<ExpressionConstraint[]>(coveringSets.size());
        for (var coveringSet : coveringSets) {
            var constraints = new ArrayList<ExpressionConstraint>(coveringSet.size());
            var coveringSetIterator = coveringSet.intIterator();
            while (coveringSetIterator.hasNext()) {
                var subsetIndex = coveringSetIterator.nextInt();
                var subsetConstraints = subsetMap.get(subsetIndex);
                Collections.addAll(constraints, subsetConstraints);
            }
            result.add(constraints.toArray(new ExpressionConstraint[0]));
        }
        return result;
    }

    private String @Nullable [] readTargetNodes() throws IOException {
        if (options.targetListFile == null) return null;
        var nodesList = new DynamicTextFileReader().read(options.targetListFile);
        return nodesList.toArray(new String[0]);
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
}
