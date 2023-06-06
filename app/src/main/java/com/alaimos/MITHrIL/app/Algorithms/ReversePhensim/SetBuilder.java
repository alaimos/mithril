package com.alaimos.MITHrIL.app.Algorithms.ReversePhensim;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

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
    private List<String> indexToTargetNode;
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
        for (var target : targetNodes) {
            log.info("Computing coverage from {}", target);
            var targetReverseIndex = reverseId2Index.getInt(target);
            var targetReverseActivity = reverseActivities[targetReverseIndex];
            if (targetReverseActivity == 0.0) continue;
            try {
                var simulationOutput = runFastPhensim(target, targetReverseActivity);
                var coveredNodes = collectCoveredNodes(simulationOutput);
                if (coveredNodes.size() > 0) {
                    indexToTargetNode.add(target);
                    output.add(coveredNodes);
                }
            } catch (IOException e) {
                log.error("Error while running FastPhensim", e);
            }
        }
    }

    /**
     * Get the output of the algorithm.
     *
     * @return the output of the algorithm.
     */
    public Pair<List<String>, List<IntSet>> output() {
        return Pair.of(indexToTargetNode, output);
    }

    //region Utility methods
    private FastPHENSIM.SimulationOutput runFastPhensim(String node, double activityScore) throws IOException {
        var constraints = new ExpressionConstraint[]{
                ExpressionConstraint.of(
                        node,
                        activityScore > 0 ? RandomExpressionGenerator.ExpressionDirection.OVEREXPRESSION : RandomExpressionGenerator.ExpressionDirection.UNDEREXPRESSION
                )
        };
        try (var phensim = new FastPHENSIM()) {
            phensim.constraints(constraints)
                   .nonExpressedNodes(nonExpressedNodes)
                   .repository(forwardRepository)
                   .repositoryMatrix(forwardRepositoryMatrix)
                   .matrixFactory(matrixFactory)
                   .random(random)
                   .batchSize(batchSize)
                   .numberOfRepetitions(1)
                   .numberOfSimulations(1)
                   .threads(threads)
                   .epsilon(epsilon)
                   .enablePValues(false)
                   .silent(true)
                   .run();
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
