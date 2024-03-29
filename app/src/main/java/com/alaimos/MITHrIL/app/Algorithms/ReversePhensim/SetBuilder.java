package com.alaimos.MITHrIL.app.Algorithms.ReversePhensim;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class SetBuilder implements Runnable {

    //region Constants
    private static final Logger log = LoggerFactory.getLogger(SetBuilder.class);
    //endregion
    //region Input Parameters
    private Random random;
    private ExpressionConstraint[] nodesToCover;
    private String[] nonExpressedNodes;
    private Repository forwardRepository;
    private RepositoryMatrix forwardRepositoryMatrix;
    private RepositoryMatrix reverseRepositoryMatrix;
    private FastPHENSIM.SimulationOutput reverseOutput;
    private String[] targetNodes;
    private int batchSize = 1000;
    private int threads = 0;
    private double epsilon;
    private MatrixFactoryInterface<?> matrixFactory;
    //endregion

    //region Internal variables
    private List<ExpressionConstraint[]> indexToTargetNode;
    private List<IntSet> output;
    //endregion

    //region Constructors and setters
    public SetBuilder() {
    }

    public SetBuilder random(Random random) {
        this.random = random;
        return this;
    }

    public SetBuilder nodesToCover(ExpressionConstraint[] constraints) {
        this.nodesToCover = constraints;
        return this;
    }

    public SetBuilder nonExpressedNodes(String[] nonExpressedNodes) {
        this.nonExpressedNodes = nonExpressedNodes;
        return this;
    }

    public SetBuilder forwardRepository(Repository repository) {
        this.forwardRepository = repository;
        return this;
    }

    public SetBuilder forwardRepositoryMatrix(RepositoryMatrix repositoryMatrix) {
        this.forwardRepositoryMatrix = repositoryMatrix;
        return this;
    }

    public SetBuilder reverseRepositoryMatrix(RepositoryMatrix reverseRepositoryMatrix) {
        this.reverseRepositoryMatrix = reverseRepositoryMatrix;
        return this;
    }

    public SetBuilder batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public SetBuilder epsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public SetBuilder matrixFactory(MatrixFactoryInterface<?> matrixFactory) {
        this.matrixFactory = matrixFactory;
        return this;
    }

    public SetBuilder threads(int threads) {
        this.threads = threads;
        return this;
    }

    public SetBuilder reverseOutput(FastPHENSIM.SimulationOutput reverseOutput) {
        this.reverseOutput = reverseOutput;
        return this;
    }

    public SetBuilder targetNodes(String[] targetNodes) {
        this.targetNodes = targetNodes;
        return this;
    }
    //endregion

    /**
     * Run the MITHrIL algorithm
     */
    @Override
    public void run() {
        indexToTargetNode = new ArrayList<>();
        output            = new ArrayList<>();
        var reverseId2Index = reverseRepositoryMatrix.pathwayMatrix().id2Index();
        var reverseActivities = reverseOutput.nodeActivityScores();
        Runnable task = () -> {
            var pairs = Arrays.stream(targetNodes).parallel().map(
                    target -> {
                        log.debug("Computing coverage for {}", target);
                        var targetReverseIndex = reverseId2Index.getInt(target);
                        var targetReverseActivity = reverseActivities[targetReverseIndex];
                        if (targetReverseActivity == 0.0) return null;
                        try {
                            var fastPhensimInput = buildFastPhensimInput(target, targetReverseActivity);
                            var simulationOutput = runFastPhensim(fastPhensimInput);
                            var coveredNodes = collectCoveredNodes(simulationOutput);
                            if (coveredNodes.size() > 0) {
                                return Pair.of(fastPhensimInput, coveredNodes);
                            }
                        } catch (IOException e) {
                            log.error("Error while running FastPhensim", e);
                        }
                        return null;
                    }).filter(Objects::nonNull).toList();
            for (var pair : pairs) {
                indexToTargetNode.add(pair.left());
                output.add(pair.right());
            }
        };
        if (threads > 0) {
            try (var pool = new ForkJoinPool(threads)) {
                pool.submit(task).get();
            } catch (ExecutionException | InterruptedException e) {
                log.error("Error while running FastPhensim", e);
            }
        } else {
            task.run();
        }
    }

    /**
     * Get the output of the algorithm.
     *
     * @return the output of the algorithm.
     */
    public Pair<List<ExpressionConstraint[]>, List<IntSet>> output() {
        return Pair.of(indexToTargetNode, output);
    }

    //region Utility methods

    @Contract("_, _ -> new")
    private ExpressionConstraint @NotNull [] buildFastPhensimInput(String node, double activityScore) {
        return new ExpressionConstraint[]{
                ExpressionConstraint.of(
                        node,
                        activityScore > 0 ? RandomExpressionGenerator.ExpressionDirection.OVEREXPRESSION : RandomExpressionGenerator.ExpressionDirection.UNDEREXPRESSION
                )
        };
    }

    private FastPHENSIM.SimulationOutput runFastPhensim(ExpressionConstraint[] constraints) throws IOException {
        try (var phensim = new FastPHENSIM()) {
            matrixFactory.setMaxThreads(1);
            phensim.constraints(constraints)
                   .nonExpressedNodes(nonExpressedNodes)
                   .repository(forwardRepository)
                   .repositoryMatrix(forwardRepositoryMatrix)
                   .matrixFactory(matrixFactory)
                   .random(random)
                   .batchSize(batchSize)
                   .numberOfRepetitions(1)
                   .numberOfSimulations(1)
                   .threads(1)
                   .epsilon(epsilon)
                   .enablePValues(false)
                   .silent(true)
                   .run();
            matrixFactory.setMaxThreads(threads);
            return phensim.output();
        }
    }

    private @NotNull IntSet collectCoveredNodes(FastPHENSIM.@NotNull SimulationOutput fastPhensimOutput) {
        var forwardId2Index = forwardRepositoryMatrix.pathwayMatrix().id2Index();
        var forwardActivities = fastPhensimOutput.nodeActivityScores();
        IntSet coveredNodes = new IntOpenHashSet();
        for (var nodeToCover : nodesToCover) {
            var nodeIndex = forwardId2Index.getInt(nodeToCover.nodeId());
            var activity = forwardActivities[nodeIndex];
            if (activity > 0) {
                coveredNodes.add(nodeIndex);
            } else if (activity < 0) {
                coveredNodes.add(-nodeIndex);
            }
        }
        return coveredNodes;
    }
    //endregion
}
