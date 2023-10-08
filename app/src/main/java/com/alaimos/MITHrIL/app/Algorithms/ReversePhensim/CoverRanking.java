package com.alaimos.MITHrIL.app.Algorithms.ReversePhensim;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
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

public class CoverRanking implements Runnable {

    //region Constants
    private static final Logger log = LoggerFactory.getLogger(CoverRanking.class);
    //endregion
    //region Input Parameters
    private Random random;
    private ExpressionConstraint[] reverseConstraints;
    private String[] nonExpressedNodes;
    private Repository repository;
    private RepositoryMatrix forwardRepositoryMatrix;
    private int batchSize = 1000;
    private int threads = 0;
    private double epsilon;
    private MatrixFactoryInterface<?> matrixFactory;
    private Collection<ExpressionConstraint[]> coveringSets;
    private int[] universe;
    //endregion

    //region Internal variables
    private List<RankedSet> output;
    //endregion

    //region Constructors and setters
    public CoverRanking() {
    }

    public CoverRanking random(Random random) {
        this.random = random;
        return this;
    }

    public CoverRanking reverseConstraints(ExpressionConstraint[] constraints) {
        this.reverseConstraints = constraints;
        return this;
    }

    public CoverRanking nonExpressedNodes(String[] nonExpressedNodes) {
        this.nonExpressedNodes = nonExpressedNodes;
        return this;
    }

    public CoverRanking repository(Repository repository) {
        this.repository = repository;
        return this;
    }

    public CoverRanking forwardRepositoryMatrix(RepositoryMatrix repositoryMatrix) {
        this.forwardRepositoryMatrix = repositoryMatrix;
        return this;
    }

    public CoverRanking batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public CoverRanking epsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public CoverRanking matrixFactory(MatrixFactoryInterface<?> matrixFactory) {
        this.matrixFactory = matrixFactory;
        return this;
    }

    public CoverRanking threads(int threads) {
        this.threads = threads;
        return this;
    }

    public CoverRanking coveringSets(Collection<ExpressionConstraint[]> coveringSets) {
        this.coveringSets = coveringSets;
        return this;
    }
    //endregion

    /**
     * Run the ranking algorithm
     */
    @Override
    public void run() {
        output = new ArrayList<>();
        var reverseConstraintsSet = convertReverseConstraintsToSet();
        Runnable task = () -> coveringSets.parallelStream().forEachOrdered(coveringSet -> {
            log.debug(
                    "Computing coverage for {}",
                    Arrays.stream(coveringSet).map(ExpressionConstraint::nodeId).toArray()
            );
            try {
                var simulationOutput = runFastPhensim(coveringSet);
                var coveredNodes = collectCoveredNodes(simulationOutput);
                var coverage = coverage(coveredNodes, reverseConstraintsSet);
                var coveredNodeConstraints = convertCoveredNodesToExpressionConstraint(coveredNodes);
                output.add(new RankedSet(coveringSet, coveredNodeConstraints, coverage));
            } catch (IOException e) {
                log.error("Error while running FastPhensim", e);
            }
        });
        if (threads > 0) {
            try (var pool = new ForkJoinPool(threads)) {
                pool.submit(task).get();
            } catch (ExecutionException | InterruptedException e) {
                log.error("Error while running FastPhensim", e);
            }
        } else {
            task.run();
        }
        Collections.sort(output);
    }

    /**
     * Get the output of the algorithm.
     *
     * @return the output of the algorithm.
     */
    public List<RankedSet> output() {
        return output;
    }

    //region Utility methods
    private FastPHENSIM.SimulationOutput runFastPhensim(ExpressionConstraint[] constraints) throws IOException {
        try (var phensim = new FastPHENSIM()) {
            matrixFactory.setMaxThreads(1);
            phensim.constraints(constraints)
                   .nonExpressedNodes(nonExpressedNodes)
                   .repository(repository)
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

    private @NotNull IntSet convertReverseConstraintsToSet() {
        var id2Index = forwardRepositoryMatrix.pathwayMatrix().id2Index();
        IntSet set = new IntOpenHashSet();
        for (var constraint : reverseConstraints) {
            var direction = constraint.direction();
            var nodeIndex = id2Index.getInt(constraint.nodeId());
            set.add(direction == RandomExpressionGenerator.ExpressionDirection.OVEREXPRESSION ? nodeIndex : -nodeIndex);
        }
        return set;
    }

    private @NotNull IntSet collectCoveredNodes(FastPHENSIM.@NotNull SimulationOutput fastPhensimOutput) {
        var id2Index = forwardRepositoryMatrix.pathwayMatrix().id2Index();
        var activities = fastPhensimOutput.nodeActivityScores();
        IntSet coveredNodes = new IntOpenHashSet();
        for (var constraint : reverseConstraints) {
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

    private ExpressionConstraint @NotNull [] convertCoveredNodesToExpressionConstraint(@NotNull IntSet coveredNodes) {
        var index2Id = forwardRepositoryMatrix.pathwayMatrix().index2Id();
        var constraints = new ExpressionConstraint[coveredNodes.size()];
        var coveredNodesIterator = coveredNodes.intIterator();
        var i = 0;
        while (coveredNodesIterator.hasNext()) {
            var nodeIndex = coveredNodesIterator.nextInt();
            var nodeId = index2Id.get(nodeIndex < 0 ? -nodeIndex : nodeIndex);
            var direction = nodeIndex < 0 ? RandomExpressionGenerator.ExpressionDirection.UNDEREXPRESSION : RandomExpressionGenerator.ExpressionDirection.OVEREXPRESSION;
            constraints[i++] = ExpressionConstraint.of(nodeId, direction);
        }
        return constraints;
    }

    private int coverage(@NotNull IntSet subset, @NotNull IntSet universe) {
        int coverage = 0;
        var subsetIterator = subset.intIterator();
        while (subsetIterator.hasNext()) {
            var element = subsetIterator.nextInt();
            if (universe.contains(element)) {
                coverage++;
            }
        }
        return coverage;
    }

    public record RankedSet(
            ExpressionConstraint[] covering,
            ExpressionConstraint[] coveredNodes,
            int coverage
    ) implements Comparable<RankedSet> {
        @Contract(pure = true)
        @Override
        public int compareTo(@NotNull RankedSet o) {
            return Integer.compare(o.coverage, coverage);
        }
    }
    //endregion
}
