package com.alaimos.MITHrIL.app.Algorithms;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator;
import com.alaimos.MITHrIL.app.Data.Generators.RandomExpressionGenerator.ExpressionConstraint;
import com.alaimos.MITHrIL.app.Data.Generators.RandomSubsetGenerator;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class PHENSIM implements Runnable, Closeable {

    //region Constants
    private static final double LOG_2 = Math.log(2);
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
    private double epsilon;
    private AdjusterInterface pValueAdjuster;
    private MatrixFactoryInterface<?> matrixFactory;

    //endregion
    //region Internal state variables
    private PartialSimulationOutput[] runPartialOutputs;
    private RandomSubsetGenerator subsetGenerator;
    private RandomExpressionGenerator expressionGenerator;
    private int currentSimulationNumber = -1;

    //endregion
    //region Constructors and setters
    public PHENSIM() {
    }

    //endregion


    /**
     * Run the MITHrIL algorithm
     */
    @Override
    public void run() {
    }

    /**
     * Get the output of the algorithm.
     *
     * @return the output of the algorithm.
     */
    public Object output() {
        return null;
    }

    //region Utility methods

    private MatrixInterface<?> prepareBatch(
            int lastBatchElement, int maxNumberOfBatches
    ) { // ((numberOfRepetitions + 1) * numberOfSimulations)
        var id2index = repositoryMatrix.pathwayMatrix().id2Index();
        var batchSize = Math.min(this.batchSize, maxNumberOfBatches - lastBatchElement);
        var batchData = new double[id2index.size() * batchSize];
        Pair<String[], double[]> expressionAssignments;
        String[] nodes;
        double[] values;
        int i, j = 0, currentSimulation;
        while (j < batchSize) {
            currentSimulation = lastBatchElement / (numberOfRepetitions + 1);
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
        return matrixFactory.of(batchData, id2index.size(), batchSize);
    }

    private int minDegree() {
        var metapathway = repository.get().graph();
        return Arrays.stream(constraints)
                     .map(c -> metapathway.node(c.nodeId()))
                     .filter(Objects::nonNull)
                     .mapToInt(metapathway::inDegree)
                     .min()
                     .orElse(0);
    }

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

    private String[] gatherCompatibleNodes(int minDegree) {
        return gatherCompatibleNodes(minDegree, 0);
    }

    private ExpressionConstraint @NotNull [] buildRandomConstraints() {
        var newSetOfIdentifiers = subsetGenerator.nextSubset(constraints.length);
        var newConstraints = new ExpressionConstraint[newSetOfIdentifiers.length];
        for (int i = 0; i < newSetOfIdentifiers.length; i++) {
            newConstraints[i] = ExpressionConstraint.of(newSetOfIdentifiers[i], constraints[i]);
        }
        return newConstraints;
    }

    private void initializeGenerators() {
        var minDegree = minDegree();
        var compatibleNodes = gatherCompatibleNodes(minDegree);
        subsetGenerator     = new RandomSubsetGenerator(compatibleNodes, random);
        expressionGenerator = new RandomExpressionGenerator(random, epsilon);
    }

    private void fillExpressionGenerator(int simulationNumber) {
        if (currentSimulationNumber != simulationNumber) {
            currentSimulationNumber = simulationNumber;
            expressionGenerator.constraints(simulationNumber == 0 ? constraints : buildRandomConstraints());
        }
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
    }

    //endregion


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

        static class PartialResultContainer {
            public static final double LOG_0_5 = -0.6931471805599453;
            private double[] perturbationAvg = null;
            private double[] perturbationSD = null;
            private double[][] counters = null;
            private double[] activityScores = null;
            private int numberOfRepetitions = 0;
            private final double epsilon;

            public PartialResultContainer(double epsilon) {
                this.epsilon = epsilon;
            }

            public void init(int size) {
                perturbationAvg     = new double[size];
                perturbationSD      = new double[size];
                counters            = new double[size][3];
                activityScores      = null;
                numberOfRepetitions = 0;
            }

            public void append(double @NotNull [] partialResults) {
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

            public void finalizeComputation() {
                activityScores = new double[perturbationSD.length];
                double LogP;
                var prior = 1d / (numberOfRepetitions * 1000d);
                var countTotal = Math.log(numberOfRepetitions + 3 * prior);
                for (var i = 0; i < perturbationSD.length; i++) {
                    perturbationSD[i] = Math.sqrt(perturbationSD[i] / (numberOfRepetitions - 1));
                    counters[i][0]    = Math.log(counters[i][0] + prior) - countTotal;
                    counters[i][1]    = Math.log(counters[i][1] + prior) - countTotal;
                    counters[i][2]    = Math.log(counters[i][2] + prior) - countTotal;
                    if (counters[i][0] > LOG_0_5) {
                        LogP = counters[i][0];
                    } else if (counters[i][1] > LOG_0_5) {
                        LogP = counters[i][1];
                    } else if (counters[i][2] > LOG_0_5) {
                        activityScores[i] = 0;
                        continue;
                    } else {
                        activityScores[i] = Double.NaN;
                        continue;
                    }
                    activityScores[i] = LogP - Math.log1p(-1 - Math.expm1(LogP));
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

        }
    }

}
