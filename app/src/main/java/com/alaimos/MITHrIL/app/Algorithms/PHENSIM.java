package com.alaimos.MITHrIL.app.Algorithms;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.ContextualisedMatrixBuilder;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Generators.RandomSubsetGenerator;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.logging.ProgressLogger;
import org.apache.commons.math3.util.FastMath;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class PHENSIM implements Runnable, Closeable {

    //region Constants
    private static final Logger log = LoggerFactory.getLogger(PHENSIM.class);
    //endregion
    //region Input Parameters
    private Random random;
    private ExpressionConstraint[] constraints;
    private String[] nonExpressedNodes;
    private Repository repository;
    private RepositoryMatrix repositoryMatrix;
    private MatrixInterface<?> repositoryMatrixTransposed = null;
    private int numberOfRepetitions = 1000;
    private int numberOfSimulations = 1000;
    private int batchSize = 1000;
    private int threads = 0;
    private double epsilon;
    private AdjusterInterface pValueAdjuster;
    private MatrixFactoryInterface<?> matrixFactory;

    //endregion
    //region Internal state variables
    private MatrixInterface<?> contextualizedMatrix = null;
    private PartialSimulationOutput[] runPartialOutputs = null;
    private RandomSubsetGenerator subsetGenerator;
    private RandomExpressionGenerator expressionGenerator;
    private int currentSimulationNumber = -1;
    private double[] nodePValues;
    private double[] nodePValuesAdjusted;
    private double[] pathwayPValues;
    private double[] pathwayPValuesAdjusted;

    //endregion
    //region Constructors and setters
    public PHENSIM() {
    }

    public PHENSIM random(Random random) {
        this.random = random;
        return this;
    }

    public PHENSIM constraints(ExpressionConstraint[] constraints) {
        this.constraints = constraints;
        return this;
    }

    public PHENSIM nonExpressedNodes(String[] nonExpressedNodes) {
        this.nonExpressedNodes = nonExpressedNodes;
        return this;
    }

    public PHENSIM repository(Repository repository) {
        this.repository = repository;
        return this;
    }

    public PHENSIM repositoryMatrix(RepositoryMatrix repositoryMatrix) {
        this.repositoryMatrix = repositoryMatrix;
        return this;
    }

    public PHENSIM numberOfRepetitions(int numberOfRepetitions) {
        this.numberOfRepetitions = numberOfRepetitions;
        return this;
    }

    public PHENSIM numberOfSimulations(int numberOfSimulations) {
        this.numberOfSimulations = numberOfSimulations;
        return this;
    }

    public PHENSIM batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public PHENSIM epsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    public PHENSIM pValueAdjuster(AdjusterInterface pValueAdjuster) {
        this.pValueAdjuster = pValueAdjuster;
        return this;
    }

    public PHENSIM matrixFactory(MatrixFactoryInterface<?> matrixFactory) {
        this.matrixFactory = matrixFactory;
        return this;
    }

    public PHENSIM threads(int threads) {
        this.threads = threads;
        return this;
    }
    //endregion

    /**
     * Run the MITHrIL algorithm
     */
    @Override
    public void run() {
        init();
        var lastBatchElement = 0;
        var totalBatchElements = (numberOfSimulations + 1) * numberOfRepetitions;
        var pl = new ProgressLogger(log, 1, TimeUnit.MINUTES, "iterations");
        pl.start("Starting iterations");
        do {
            var batchPair = prepareBatch(lastBatchElement, totalBatchElements);
            var columnToSimulationMap = batchPair.right();
            try (
                    var batch = batchPair.left();
                    var batchNodePerturbations = computeBatchPerturbations(batch);
                    var batchPathwayPerturbations = computePathwayPerturbations(batchNodePerturbations)
            ) {
                batchNodePerturbations.column(0);
                batchPathwayPerturbations.column(0);
                if (threads > 0) {
                    try (var pool = new ForkJoinPool(threads)) {
                        pool.submit(() -> {
                            IntStream.range(0, columnToSimulationMap.length).parallel().forEach(i -> {
                                var resultsContainer = runPartialOutputs[columnToSimulationMap[i]];
                                var nodePerturbations = batchNodePerturbations.column(i);
                                var pathwayPerturbations = batchPathwayPerturbations.column(i);
                                resultsContainer.append(nodePerturbations, pathwayPerturbations);
                            });
                        }).get();
                    } catch (ExecutionException | InterruptedException e) {
                        log.error("Error while running parallel computation of counters", e);
                        throw new RuntimeException(e);
                    }
                } else {
                    IntStream.range(0, columnToSimulationMap.length).parallel().forEach(i -> {
                        var resultsContainer = runPartialOutputs[columnToSimulationMap[i]];
                        var nodePerturbations = batchNodePerturbations.column(i);
                        var pathwayPerturbations = batchPathwayPerturbations.column(i);
                        resultsContainer.append(nodePerturbations, pathwayPerturbations);
                    });
                }
                lastBatchElement += columnToSimulationMap.length;
                pl.update(columnToSimulationMap.length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (lastBatchElement < totalBatchElements);
        pl.done();
        log.info("Computing activity scores");
        computeActivityScores();
        log.info("Computing p-values");
        computeNodePValues();
        computePathwayPValues();
    }

    /**
     * Get the output of the algorithm.
     *
     * @param appendAllRuns if true, all the runs will be appended to the output, otherwise only the first run will be
     *                      returned
     * @return the output of the algorithm.
     */
    public SimulationOutput output(boolean appendAllRuns) {
        return new SimulationOutput(
                appendAllRuns ? runPartialOutputs : new PartialSimulationOutput[]{runPartialOutputs[0]},
                nodePValues, nodePValuesAdjusted, pathwayPValues, pathwayPValuesAdjusted
        );
    }

    //region Utility methods

    /**
     * Initialize the matrices, generators, output containers, and other internal variables.
     */
    private void init() {
        if (repositoryMatrixTransposed == null) {
            repositoryMatrixTransposed = repositoryMatrix.matrix().transpose();
        }
        if (runPartialOutputs == null) {
            runPartialOutputs = new PartialSimulationOutput[numberOfSimulations + 1];
            for (var i = 0; i < runPartialOutputs.length; i++) {
                runPartialOutputs[i] = new PartialSimulationOutput(epsilon);
            }
        }
        var numberOfNodes = repositoryMatrix.pathwayMatrix().id2Index().size();
        var numberOfPathways = repositoryMatrix.id2Index().size();
        for (PartialSimulationOutput runPartialOutput : runPartialOutputs) {
            runPartialOutput.init(numberOfNodes, numberOfPathways);
        }
        initializeGenerators();
        log.info("Building contextualized metapathway matrix");
        contextualizedMatrix = ContextualisedMatrixBuilder.build(
                repository, repositoryMatrix, matrixFactory, nonExpressedNodes, epsilon);
    }

    /**
     * Prepares a matrix containing the input for a batch of runs.
     *
     * @param lastBatchElement         the index of the first element of the batch
     * @param totalNumberOfRepetitions the total number of repetitions (runs)
     * @return a matrix containing the input for a batch of runs, and an array containing the mapping between matrix
     * columns and result containers.
     */
    private @NotNull Pair<MatrixInterface<?>, int[]> prepareBatch(
            int lastBatchElement, int totalNumberOfRepetitions
    ) {
        var id2index = repositoryMatrix.pathwayMatrix().id2Index();
        var batchSize = FastMath.min(this.batchSize, totalNumberOfRepetitions - lastBatchElement);
        var batchData = new double[id2index.size() * batchSize];
        var batchToSimulation = new int[batchSize];
        Pair<String[], double[]> expressionAssignments;
        String[] nodes;
        double[] values;
        int i, j = 0, currentSimulation;
        while (j < batchSize) {
            batchToSimulation[j] = currentSimulation = lastBatchElement / numberOfRepetitions;
            fillExpressionGenerator(currentSimulation);
            expressionAssignments = expressionGenerator.nextRandomExpression();
            nodes                 = expressionAssignments.left();
            values                = expressionAssignments.right();
            for (var k = 0; k < nodes.length; k++) {
                i = id2index.getOrDefault(nodes[k], -1);
                if (i < 0) continue;
                batchData[i * batchSize + j] = values[k];
            }
            lastBatchElement++;
            j++;
        }
        return Pair.of(matrixFactory.of(batchData, id2index.size(), batchSize), batchToSimulation);
    }

    /**
     * Given a batch of data, this method computes the perturbations of the batch. Given a run, the perturbation is
     * computed as contextualizedPathwayMatrix * run, where pathwayMatrix is computed as (I-W)^-1 and contextualized
     * using the non-expressed elements provided as input.
     *
     * @param batch the batch of data
     * @return the perturbations of the batch stored in a matrix, where each row is a gene, and each column is a run.
     */
    private MatrixInterface<?> computeBatchPerturbations(@NotNull MatrixInterface<?> batch) {
        return batch.preMultiply(contextualizedMatrix);
    }

    /**
     * Given a batch perturbations, this method computes the pathway perturbations of the batch. Given a run, the
     * pathway perturbation is computed as repositoryMatrix^T * run, where ^T is the transpose operator. The
     * repositoryMatrix is a matrix where each row is a gene, and each column is a pathway. Position (i,j) is 1 if gene
     * i is in pathway j, 0 otherwise.
     *
     * @param batchPerturbation the perturbations of a batch computed with computeBatchPerturbations
     * @return the pathway perturbations of the batch stored in a matrix, where each row is a pathway, and each column
     * is a run.
     */
    private MatrixInterface<?> computePathwayPerturbations(@NotNull MatrixInterface<?> batchPerturbation) {
        return batchPerturbation.preMultiply(repositoryMatrixTransposed);
    }

    /**
     * Given the user input, this method computes the minimum out-degree of the nodes matching the input constraints.
     *
     * @return the minimum out-degree of the nodes matching the input constraints.
     */
    private int minDegree() {
        var metapathway = repository.get().graph();
        return Arrays.stream(constraints)
                     .map(c -> metapathway.node(c.nodeId()))
                     .filter(Objects::nonNull)
                     .mapToInt(metapathway::inDegree)
                     .min()
                     .orElse(0);
    }

    /**
     * Given the user input, this method computes the set of nodes matching with the network connectivity of the input
     * nodes. If the number of nodes is less than the number of repetitions, this method will try to increase the number
     * of nodes by decreasing the minimum out-degree of the nodes matching the input constraints. If the number of nodes
     * is still less than the number of repetitions, this method will return all the nodes.
     *
     * @param minDegree the minimum out-degree of the nodes matching the input constraints.
     * @param count     the number of times this method has been called recursively.
     * @return an array of node identifiers matching with the network connectivity of the input nodes.
     */
    private String[] gatherCompatibleNodes(int minDegree, int count) {
        var allNodes = new HashSet<String>();
        var metapathway = repository.get().graph();
        for (var n : metapathway.nodes().values()) {
            if (metapathway.inDegree(n) >= minDegree) {
                allNodes.add(n.id());
            }
        }
        var s = numberOfRepetitions + 1;
        if (count < 100 && allNodes.size() < s && minDegree > 0) {
            return gatherCompatibleNodes(minDegree - 1, count + 1);
        }
        if (count == 100 && minDegree > 0) {
            return gatherCompatibleNodes(0, count + 1);
        }
        return allNodes.toArray(new String[0]);
    }

    /**
     * Given the user input, this method computes the set of nodes matching with the network connectivity of the input
     * nodes.
     *
     * @param minDegree the minimum out-degree of the nodes matching the input constraints.
     * @return an array of node identifiers matching with the network connectivity of the input nodes.
     */
    private String[] gatherCompatibleNodes(int minDegree) {
        return gatherCompatibleNodes(minDegree, 0);
    }

    /**
     * Given the user input, this method computes a random set of constraints.
     *
     * @return a random set of constraints.
     */
    private ExpressionConstraint @NotNull [] buildRandomConstraints() {
        var newSetOfIdentifiers = subsetGenerator.nextSubset(constraints.length);
        var newConstraints = new ExpressionConstraint[newSetOfIdentifiers.length];
        for (int i = 0; i < newSetOfIdentifiers.length; i++) {
            newConstraints[i] = ExpressionConstraint.of(newSetOfIdentifiers[i], constraints[i]);
        }
        return newConstraints;
    }

    /**
     * This method initializes the generators used by the algorithm.
     */
    private void initializeGenerators() {
        var minDegree = minDegree();
        var compatibleNodes = gatherCompatibleNodes(minDegree);
        subsetGenerator     = new RandomSubsetGenerator(compatibleNodes, random);
        expressionGenerator = new RandomExpressionGenerator(random, epsilon);
    }

    /**
     * This method fills the expression generator with the constraints of the next simulation. If the number of the next
     * simulation is the same as the current one, this method does nothing. If the next simulation is the first one,
     * this method fills the expression generator with the user constraints; otherwise it uses random constraints.
     *
     * @param simulationNumber the number of the next simulation.
     */
    private void fillExpressionGenerator(int simulationNumber) {
        if (currentSimulationNumber != simulationNumber) {
            currentSimulationNumber = simulationNumber;
            expressionGenerator.constraints(simulationNumber == 0 ? constraints : buildRandomConstraints());
        }
    }

    /**
     *
     */
    private void computeActivityScores() {
        for (var run : runPartialOutputs) {
            run.finalizeComputation();
        }
    }

    /**
     * This method computes the p-values of the nodes in the network.
     */
    private void computeNodePValues() {
        var reference = runPartialOutputs[0].nodeResultsContainer.activityScores;
        nodePValues = new double[reference.length];
        int counter;
        double val, ref;
        for (var node = 0; node < reference.length; node++) {
            counter = 0;
            ref     = reference[node];
            for (var run = 1; run < runPartialOutputs.length; run++) {
                val = runPartialOutputs[run].nodeResultsContainer.activityScores[node];
                if (Double.isNaN(ref) ||
                        (ref < 0 && val <= ref) ||
                        (ref > 0 && val >= ref) ||
                        (ref == 0 && val == 0)) {
                    counter++;
                }
            }
            nodePValues[node] = ((double) counter) / numberOfSimulations;
        }
        nodePValuesAdjusted = pValueAdjuster.adjust(nodePValues);
    }

    /**
     * This method computes the p-values of the pathways.
     */
    private void computePathwayPValues() {
        var reference = runPartialOutputs[0].pathwayResultsContainer.activityScores;
        pathwayPValues = new double[reference.length];
        int counter;
        double val, ref;
        for (var pathway = 0; pathway < reference.length; pathway++) {
            counter = 0;
            ref     = reference[pathway];
            for (var run = 1; run < runPartialOutputs.length; run++) {
                val = runPartialOutputs[run].pathwayResultsContainer.activityScores[pathway];
                if (Double.isNaN(ref) ||
                        (ref < 0 && val <= ref) ||
                        (ref > 0 && val >= ref) ||
                        (ref == 0 && val == 0)) {
                    counter++;
                }
            }
            pathwayPValues[pathway] = ((double) counter) / numberOfSimulations;
        }
        pathwayPValuesAdjusted = pValueAdjuster.adjust(pathwayPValues);
    }

    /**
     * Free the resources used by the algorithm.
     *
     * @throws IOException this exception is never thrown, but it is required by the {@link AutoCloseable} interface.
     */
    @Override
    public void close() throws IOException {
        if (repositoryMatrixTransposed != null) {
            repositoryMatrixTransposed.close();
        }
        if (contextualizedMatrix != null && contextualizedMatrix != repositoryMatrix.pathwayMatrix().matrix()) {
            contextualizedMatrix.close();
        }
    }

    //endregion
    //region INNER CLASSES
    public static class PartialSimulationOutput {

        private final PartialResultContainer nodeResultsContainer;
        private final PartialResultContainer pathwayResultsContainer;

        public PartialSimulationOutput(double epsilon) {
            nodeResultsContainer    = new PartialResultContainer(epsilon);
            pathwayResultsContainer = new PartialResultContainer(epsilon);
        }

        public void init(int numberOfNodes, int numberOfPathways) {
            nodeResultsContainer.init(numberOfNodes);
            pathwayResultsContainer.init(numberOfPathways);
        }

        public void append(double @NotNull [] nodePerturbations, double @NotNull [] pathwayPerturbations) {
            nodeResultsContainer.append(nodePerturbations);
            pathwayResultsContainer.append(pathwayPerturbations);
        }

        public int numberOfRepetitions() {
            return nodeResultsContainer.numberOfRepetitions;
        }

        public double[] nodePerturbationsAverage() {
            return nodeResultsContainer.perturbationAverage();
        }

        public double[] nodePerturbationsStdDev() {
            return nodeResultsContainer.perturbationStdDev();
        }

        public double[] nodeActivityScores() {
            return nodeResultsContainer.activityScores();
        }

        public double[] pathwayPerturbationsAverage() {
            return pathwayResultsContainer.perturbationAverage();
        }

        public double[] pathwayPerturbationsStdDev() {
            return pathwayResultsContainer.perturbationStdDev();
        }

        public double[] pathwayActivityScores() {
            return pathwayResultsContainer.activityScores();
        }

        private void finalizeComputation() {
            nodeResultsContainer.finalizeComputation();
            pathwayResultsContainer.finalizeComputation();
        }

        static class PartialResultContainer {
            public static final double LOG_0_5 = -0.6931471805599453;
            private double[] perturbationAvg = null;
            private double[] perturbationSD = null;
            private int[][] counters = null;
            private double[] activityScores = null;
            private int numberOfRepetitions = 0;
            private final double epsilon;

            public PartialResultContainer(double epsilon) {
                this.epsilon = epsilon;
            }

            public void init(int size) {
                perturbationAvg     = new double[size];
                perturbationSD      = new double[size];
                counters            = new int[size][3];
                activityScores      = null;
                numberOfRepetitions = 0;
            }

            public synchronized void append(double @NotNull [] partialResults) {
                numberOfRepetitions++;
                double delta, delta2, newValue;
                for (var i = 0; i < partialResults.length; i++) {
                    newValue = partialResults[i];
                    delta    = newValue - perturbationAvg[i];
                    perturbationAvg[i] += delta / numberOfRepetitions;
                    delta2   = newValue - perturbationAvg[i];
                    perturbationSD[i] += delta * delta2;
                    if (newValue > epsilon) {
                        counters[i][0]++;
                    } else if (newValue < -epsilon) {
                        counters[i][1]++;
                    } else {
                        counters[i][2]++;
                    }
                }
            }

            public double[] perturbationAverage() {
                if (activityScores == null) {
                    finalizeComputation();
                }
                return perturbationAvg;
            }

            public double[] perturbationStdDev() {
                if (activityScores == null) {
                    finalizeComputation();
                }
                return perturbationSD;
            }

            public double[] activityScores() {
                if (activityScores == null) {
                    finalizeComputation();
                }
                return activityScores;
            }

            private void finalizeComputation() {
                activityScores = new double[perturbationSD.length];
                double probActivation, probInhibition, probNoChange;
                var nm1 = numberOfRepetitions - 1;
                var prior = 1d / (numberOfRepetitions * 1000d);
                var countTotal = Math.log(numberOfRepetitions + 3 * prior);
                for (var i = 0; i < perturbationSD.length; i++) {
                    perturbationSD[i] = Math.sqrt(perturbationSD[i] / nm1);
                    probActivation    = Math.log(counters[i][0] + prior) - countTotal;
                    probInhibition    = Math.log(counters[i][1] + prior) - countTotal;
                    probNoChange      = Math.log(counters[i][2] + prior) - countTotal;
                    if (probActivation > LOG_0_5) {
                        activityScores[i] = probActivation - Math.log1p(-1 - Math.expm1(probActivation));
                    } else if (probInhibition > LOG_0_5) {
                        activityScores[i] = Math.log1p(-1 - Math.expm1(probInhibition)) - probInhibition;
                    } else if (probNoChange > LOG_0_5) {
                        activityScores[i] = 0;
                    } else {
                        activityScores[i] = Double.NaN;
                    }
                }
            }
        }
    }

    public record SimulationOutput(
            PartialSimulationOutput[] runs,
            double[] nodePValues,
            double[] nodePValuesAdjusted,
            double[] pathwayPValues,
            double[] pathwayPValuesAdjusted
    ) {

        public double[] nodePerturbationsAverage() {
            return runs[0].nodePerturbationsAverage();
        }

        public double[] nodePerturbationsStdDev() {
            return runs[0].nodePerturbationsStdDev();
        }

        public double[] nodeActivityScores() {
            return runs[0].nodeActivityScores();
        }

        public double[] pathwayPerturbationsAverage() {
            return runs[0].pathwayPerturbationsAverage();
        }

        public double[] pathwayPerturbationsStdDev() {
            return runs[0].pathwayPerturbationsStdDev();
        }

        public double[] pathwayActivityScores() {
            return runs[0].pathwayActivityScores();
        }

        public int[][] nodeCounters() {
            return runs[0].nodeResultsContainer.counters;
        }

        public int[][] pathwayCounters() {
            return runs[0].pathwayResultsContainer.counters;
        }
    }
    //endregion
}
