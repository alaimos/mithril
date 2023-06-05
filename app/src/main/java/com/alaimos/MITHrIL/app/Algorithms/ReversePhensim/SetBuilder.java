package com.alaimos.MITHrIL.app.Algorithms.ReversePhensim;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.ContextualisedMatrixBuilder;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Generators.RandomSubsetGenerator;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.logging.ProgressLogger;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class SetBuilder implements Runnable {

    //region Constants
    private static final Logger log = LoggerFactory.getLogger(SetBuilder.class);
    //endregion
    //region Input Parameters
    private Random random;
    private ExpressionConstraint[] inputConstraints;
    private String[] nonExpressedNodes;
    private Repository repository;
    private RepositoryMatrix repositoryMatrix;
    private RepositoryMatrix reverseRepositoryMatrix;
    private int batchSize = 1000;
    private int threads = 0;
    private double epsilon;
    private MatrixFactoryInterface<?> matrixFactory;
    private FastPHENSIM.SimulationOutput reverseOutput;
    private String[] targetNodes;
    //endregion

    //region Internal variables
    private List<IntSet> output;
    //endregion

    //region Constructors and setters
    public SetBuilder() {
    }

    public SetBuilder random(Random random) {
        this.random = random;
        return this;
    }

    public SetBuilder inputConstraints(ExpressionConstraint[] constraints) {
        this.inputConstraints = constraints;
        return this;
    }

    public SetBuilder nonExpressedNodes(String[] nonExpressedNodes) {
        this.nonExpressedNodes = nonExpressedNodes;
        return this;
    }

    public SetBuilder repository(Repository repository) {
        this.repository = repository;
        return this;
    }

    public SetBuilder repositoryMatrix(RepositoryMatrix repositoryMatrix) {
        this.repositoryMatrix = repositoryMatrix;
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
        output = new ArrayList<>();
        var id2Index = reverseRepositoryMatrix.pathwayMatrix().id2Index();
        var activities = reverseOutput.nodeActivityScores();
        for (var target : targetNodes) {
            log.info("Computing coverage from {}", target);
            var targetIndex = id2Index.getInt(target);
            var targetActivity = activities[targetIndex];
            try {
                var simulationOutput = runFastPhensim(target, targetActivity);
                var set = collectCoveredNodes(simulationOutput);
                if (set.size() > 0) {
                    output.add(set);
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
    public List<IntSet> output() {
        return output;
    }

    //region Utility methods
    private FastPHENSIM.SimulationOutput runFastPhensim(String node, double activityScore) throws IOException {
        var constraints = new ExpressionConstraint[]{
                new ExpressionConstraint(
                        node,
                        activityScore > 0 ? RandomExpressionGenerator.ExpressionDirection.OVEREXPRESSION : RandomExpressionGenerator.ExpressionDirection.UNDEREXPRESSION,
                        null, Double.NaN
                )
        };
        try (var phensim = new FastPHENSIM()) {
            phensim.constraints(constraints)
                   .nonExpressedNodes(nonExpressedNodes)
                   .repository(repository)
                   .repositoryMatrix(repositoryMatrix)
                   .matrixFactory(matrixFactory)
                   .random(random)
                   .batchSize(batchSize)
                   .numberOfRepetitions(1)
                   .numberOfSimulations(1)
                   .threads(threads)
                   .epsilon(epsilon)
                   .enablePValues(false)
                   .run();
            return phensim.output();
        }
    }

    private @NotNull IntSet collectCoveredNodes(FastPHENSIM.@NotNull SimulationOutput fastPhensimOutput) {
        var id2Index = repositoryMatrix.pathwayMatrix().id2Index();
        var activities = fastPhensimOutput.nodeActivityScores();
        IntSet coveredNodes = new IntOpenHashSet();
        for (var constraint : inputConstraints) {
            var nodeIndex = id2Index.getInt(constraint.nodeId());
            var activity = activities[nodeIndex];
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