package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.AbstractOptions;
import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.BenjaminiHochberg;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MatrixBuilderFromMetapathway;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MetapathwayBuilderFromOptions;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM;
import com.alaimos.MITHrIL.app.Algorithms.ReversePhensim.FastPHENSIM;
import com.alaimos.MITHrIL.app.CommandLine.Options.MetapathwayOptions;
import com.alaimos.MITHrIL.app.CommandLine.Options.ReversePHENSIMOptions;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionDirection;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class TestReversePHENSIMService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(TestReversePHENSIMService.class);

    protected Options options = new Options();

    @Override
    public String getShortName() {
        return "test-reverse-phensim";
    }

    @Override
    public String getDescription() {
        return "runs the tests of the reverse PHENSIM algorithm";
    }

    @Override
    public OptionsInterface getOptions() {
        return options;
    }

    @Override
    public void run() {
        try {
            Configurator.setRootLevel(Level.INFO);
            var outDir = options.output == null ? new File("./") : options.output;
            outDir.mkdirs();
            var random = new Random(1234);
            var metapathwayRepository = MetapathwayBuilderFromOptions.build(new MetapathwayOptions(), random);
            var inversionMatrixFactory = matrixFactory(options.inversionFactory);
            var metapathwayMatrix = MatrixBuilderFromMetapathway.build(metapathwayRepository, inversionMatrixFactory);
            try (PrintStream ps = new PrintStream(new FileOutputStream(new File(outDir, "metapathway_map.txt")))) {
                metapathwayMatrix.id2Index().forEach((k, v) -> ps.println(k + "\t" + v));
            }
            log.info("Creating random inputs");
            var randomStartingNodes = randomInputGenerator(metapathwayRepository, random);
            try (PrintStream ps = new PrintStream(new FileOutputStream(new File(outDir, "random_starting.txt")))) {
                for (var i = 0; i < randomStartingNodes.size(); i++) {
                    var nodes = Arrays.stream(randomStartingNodes.get(i))
                                      .map(n -> n.nodeId() + ";" + n.direction())
                                      .collect(Collectors.joining(","));
                    ps.println(i + "\t" + nodes);
                }
            }
            log.info("Running PHENSIM on starting nodes");
            var startingResults = runPhensim(randomStartingNodes, metapathwayRepository, random, metapathwayMatrix);
            log.info("Building Reverse PHENSIM input");
            var params = buildParameterList();
            for (var i = 0; i < randomStartingNodes.size(); i++) {
                log.info("Writing inputs for random input {}/{}", i + 1, randomStartingNodes.size());
                var dir = new File(outDir, "random_" + i);
                dir.mkdirs();
                var forwardInput = randomStartingNodes.get(i);
                var forwardOutput = startingResults.get(i);
                for (var j = 0; j < params.size(); j++) {
                    var param = params.get(j);
                    var reverseInput = buildReversePhensimInput(
                            forwardInput, metapathwayRepository, metapathwayMatrix, forwardOutput, param, random
                    );
                    var file = new File(dir, "input_" + j + ".txt");
                    try (PrintStream ps = new PrintStream(new FileOutputStream(file))) {
                        reverseInput.forEach((k, v) -> {
                            ps.println(k + "\t" + v);
                        });
                    }
                }
            }
            log.info("Running Reverse PHENSIM");
            try (var pool = new ForkJoinPool(options.concurrentRuns)) {
                pool.submit(() -> {
                    IntStream.range(0, randomStartingNodes.size())
                             .mapToObj(i -> IntStream.range(0, params.size())
                                                     .mapToObj(j -> IntIntPair.of(i, j)))
                             .flatMap(s -> s)
                             .parallel()
                             .forEach(pair -> {
                                 var i = pair.leftInt();
                                 var j = pair.rightInt();
                                 log.info("Running Reverse PHENSIM test {} {}", i, j);
                                 var rf = new ReversePHENSIMService();
                                 var opt = (ReversePHENSIMOptions) rf.getOptions();
                                 opt.threads               = options.threads;
                                 opt.input                 = new File(outDir, "random_" + i + "/input_" + j + ".txt");
                                 opt.output                = new File(outDir, "random_" + i + "/output_" + j + ".txt");
                                 opt.epsilon               = 0.00001;
                                 opt.inversionFactory      = options.inversionFactory;
                                 opt.multiplicationFactory = options.multiplicationFactory;
                                 opt.verbose               = false;
                                 opt.iterations            = 100;
                                 opt.simulations           = 100;
                                 rf.run();
                             });
                }).get();
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error("An error occurred", e);
        }
    }

    private @NotNull List<ExpressionConstraint[]> randomInputGenerator(@NotNull Repository metapathway, Random random) {
        var graph = metapathway.get().graph();
        var endpoints = new HashSet<>(graph.endpoints());
        var nodes = graph.nodesStream()
                         .filter(n -> graph.outDegree(n) > 0)
                         .filter(n -> {
                             var i = graph.inDegree(n);
                             return i == 0 || i > 1;
                         })
                         .map(Node::id)
                         .filter(n -> !endpoints.contains(n))
                         .toArray(String[]::new);
        var numberOfNodes = nodes.length;
        var selections = new ArrayList<ExpressionConstraint[]>();
        var maxSelection = options.maxSelection;
        var directions = new ExpressionDirection[]{
                ExpressionDirection.OVEREXPRESSION, ExpressionDirection.UNDEREXPRESSION
        };
        for (var i = 0; i < maxSelection; i++) {
            var size = 1; //random.nextInt(10) + 1;
            var selectedNodes = new HashSet<String>(size);
            while (selectedNodes.size() < size) {
                var node = nodes[random.nextInt(numberOfNodes)];
                selectedNodes.add(node);
            }
            selections.add(
                    selectedNodes.stream()
                                 .map(node -> ExpressionConstraint.of(
                                         node,
                                         directions[random.nextInt(2)]
                                 ))
                                 .toArray(ExpressionConstraint[]::new)
            );
        }
        return selections;
    }

    private @NotNull List<double[]> runPhensim(
            @NotNull List<ExpressionConstraint[]> constraints, @NotNull Repository metapathway, @NotNull Random random,
            RepositoryMatrix metapathwayMatrix
    ) throws IOException {
        var multiplicationMatrixFactory = matrixFactory(options.multiplicationFactory);
        var output = new ArrayList<double[]>(constraints.size());
        for (var i = 0; i < constraints.size(); i++) {
            log.info("Running simulation {}/{}", i + 1, constraints.size());
            var input = constraints.get(i);
            Configurator.setRootLevel(Level.WARN);
            try (var phensim = new FastPHENSIM()) {
                phensim.constraints(input)
                       .nonExpressedNodes(new String[0])
                       .repository(metapathway)
                       .repositoryMatrix(metapathwayMatrix)
                       .matrixFactory(multiplicationMatrixFactory)
                       .random(random)
                       .batchSize(options.batchSize)
                       .numberOfRepetitions(1)
                       .numberOfSimulations(1000)
                       .threads(options.threads)
                       .epsilon(0.00001)
                       .enablePValues(false)
                       .run();
                output.add(phensim.output().nodeActivityScores());
            }
            Configurator.setRootLevel(Level.INFO);
        }
        return output;
    }

    private @NotNull ArrayList<double[]> buildParameterList() {
        var params = new ArrayList<double[]>();
        for (var i = 0; i < 10; i++) {
            for (var d = 0; d < 5; d++) {
                for (var s = 0; s < 5; s++) {
                    params.add(new double[]{(double) i / 10, (double) d / 10, (double) s / 10});
                }
            }
        }
        return params;
    }

    private @NotNull Map<String, ExpressionDirection> buildReversePhensimInput(
            ExpressionConstraint[] forwardInput, @NotNull Repository metapathway,
            @NotNull RepositoryMatrix metapathwayMatrix, double[] forwardOutput, double @NotNull [] params,
            Random random
    ) {
        var graph = metapathway.get().graph();
        var forwardExclusion = Arrays.stream(forwardInput)
                                     .map(ExpressionConstraint::nodeId)
                                     .collect(Collectors.toSet());
        var endpoints = graph.endpoints();
        var otherNodes = graph.nodesStream()
                              .map(Node::id)
                              .filter(n -> !(endpoints.contains(n) || forwardExclusion.contains(n)))
                              .toArray(String[]::new);
        var id2Index = metapathwayMatrix.pathwayMatrix().id2Index();
        var insertions = params[0];
        var deletions = params[1];
        var substitutions = params[2];
        var reverseInput = new HashMap<String, ExpressionDirection>();
        for (var e : endpoints) {
            var exp = forwardOutput[id2Index.getInt(e)];
            if (exp != 0.0) reverseInput.put(e, activityToConstraint(exp));
        }
        if (deletions > 0) {
            var sizeAfterDeletions = (int) Math.round(reverseInput.size() * (1.0 - deletions));
            if (sizeAfterDeletions > 0) {
                while (reverseInput.size() > sizeAfterDeletions) {
                    var nodeToDelete = endpoints.get(random.nextInt(endpoints.size()));
                    reverseInput.remove(nodeToDelete);
                }
            }
        }
        if (insertions > 0) {
            var sizeAfterInsertions = (int) Math.round(reverseInput.size() * (1.0 + insertions));
            if (sizeAfterInsertions > reverseInput.size()) {
                while (reverseInput.size() < sizeAfterInsertions) {
                    var nodeToInsert = otherNodes[random.nextInt(otherNodes.length)];
                    var exp = forwardOutput[id2Index.getInt(nodeToInsert)];
                    if (exp != 0.0) reverseInput.put(nodeToInsert, activityToConstraint(exp));
                }
            }
        }
        if (substitutions > 0) {
            var numberOfSubstitutions = (int) Math.round(reverseInput.size() * substitutions);
            if (numberOfSubstitutions > 0) {
                var keys = new ArrayList<>(reverseInput.keySet());
                var i = 0;
                while (i < numberOfSubstitutions) {
                    var nodeToSubstitute = keys.get(random.nextInt(keys.size()));
                    var activityOfNodeToSubstitute = reverseInput.get(nodeToSubstitute);
                    if (activityOfNodeToSubstitute == ExpressionDirection.OVEREXPRESSION) {
                        reverseInput.put(nodeToSubstitute, ExpressionDirection.UNDEREXPRESSION);
                    } else {
                        reverseInput.put(nodeToSubstitute, ExpressionDirection.OVEREXPRESSION);
                    }
                    i++;
                }
            }
        }
        return reverseInput;
    }

    private static ExpressionDirection activityToConstraint(double value) {
        return (value < 0) ? ExpressionDirection.UNDEREXPRESSION : ExpressionDirection.OVEREXPRESSION;
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

    private static class Options extends AbstractOptions {
        @Option(name = "-max-selection", usage = "maximum number of random input selections")
        public int maxSelection = 10;
        @Option(name = "-batch-size", usage = "number of iterations to be performed in a single batch.")
        public int batchSize = 1000;
        @Option(name = "-t", aliases = "-threads", usage = "number of threads used for matrix operations (negative to disable limit; not supported by all matrix math libraries).")
        public int threads = -1;
        @Option(name = "-c", aliases = "-concurrent-runs", usage = "number of concurrent runs for the test.")
        public int concurrentRuns = 1;
        @Option(name = "-inversion-factory", usage = "the matrix math library used to create and compute the metapathway matrix representation (see the list of \"matrix-math\" extensions for possible values).")
        public String inversionFactory = "default";
        @Option(name = "-multiplication-factory", usage = "the matrix math library used to perform the operations needed for the MITHrIL iteration (see the list of \"matrix-math\" extensions for possible values).")
        public String multiplicationFactory = "default";
        @Option(name = "-o", aliases = "-out", usage = "output directory", required = true)
        public File output = null;
    }
}
