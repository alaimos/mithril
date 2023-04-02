package com.alaimos.MITHrIL.app.Algorithms;

import com.alaimos.MITHrIL.api.Data.Pathways.Enrichment.EnrichmentProbabilityComputationInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.MatrixInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;
import com.alaimos.MITHrIL.api.Math.StreamMedian.StreamMedianComputationInterface;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import com.alaimos.MITHrIL.app.Data.Records.MITHrILOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import it.unimi.dsi.logging.ProgressLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MITHrIL implements Runnable, Closeable {

    //region Constants
    private static final double LOG_2 = Math.log(2);
    private static final Logger log = LoggerFactory.getLogger(MITHrIL.class);
    //endregion
    //region Input Parameters
    private Random random;
    private ExpressionInput input;
    private Repository repository;
    private RepositoryMatrix repositoryMatrix;
    private MatrixInterface<?> repositoryMatrixTransposed = null;
    private int numberOfRepetitions = 2001;
    private int batchSize = 1000;
    private EnrichmentProbabilityComputationInterface probabilityComputation;
    private CombinerInterface pValueCombiner;
    private AdjusterInterface pValueAdjuster;
    private MatrixFactoryInterface<?> matrixFactory;
    private Supplier<StreamMedianComputationInterface> medianAlgorithmFactory;
    private boolean noPValue = false;
    //endregion
    //region Internal state variables
    private StreamMedianComputationInterface[] medians = null;
    private double[] nodePerturbations = null;
    private double[] nodeAccumulators = null;
    private double[] pathwayAccumulators = null;
    private double[] pathwayCorrectedAccumulators = null;
    private double[] pathwayImpactFactors = null;
    private double[] pathwayProbabilities = null;
    private double[] pathwayNetworkProbabilities = null;
    private double[] nodePValues = null;
    private double[] nodeAdjustedPValues = null;
    private double[] pathwayPValues = null;
    private double[] pathwayAdjustedPValues = null;

    //endregion
    //region Constructors and setters
    public MITHrIL() {
    }

    /**
     * Given the raw accumulators of a batch, this method computes the number of events matching the p-value definition.
     * That is the number of times we observe a raw accumulator (absolute value) greater than the raw accumulator of the
     * user input (absolute value). The absolute value is not explicitly computed to avoid the overhead of the
     * Math.abs.
     *
     * @param rawAccumulators the raw accumulators of a batch
     * @param first           the first column of the batch to consider
     * @param accumulators    the accumulators of the user input
     * @param pValues         the array containing the counts to update
     */
    private static void countPValueEvents(
            @NotNull MatrixInterface<?> rawAccumulators, int first, double[] accumulators, double[] pValues
    ) {
        rawAccumulators.forEach(MatrixInterface.Direction.ROW, (v, i) -> {
            var p = accumulators[i];
            for (var j = first; j < v.length; j++) {
                if ((p > 0 && v[j] >= p) || (p < 0 && v[j] <= p)) pValues[i] += 1.0;
            }
        });
    }

    public MITHrIL random(Random random) {
        this.random = random;
        return this;
    }

    public MITHrIL input(ExpressionInput input) {
        this.input = input;
        return this;
    }

    public MITHrIL repository(Repository repository) {
        this.repository = repository;
        return this;
    }

    public MITHrIL repositoryMatrix(RepositoryMatrix repositoryMatrix) {
        this.repositoryMatrix = repositoryMatrix;
        return this;
    }

    public MITHrIL numberOfRepetitions(int numberOfRepetitions) {
        this.numberOfRepetitions = numberOfRepetitions;
        return this;
    }

    public MITHrIL batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public MITHrIL probabilityComputation(EnrichmentProbabilityComputationInterface probabilityComputation) {
        this.probabilityComputation = probabilityComputation;
        return this;
    }

    public MITHrIL pValueCombiner(CombinerInterface pValueCombiner) {
        this.pValueCombiner = pValueCombiner;
        return this;
    }

    public MITHrIL pValueAdjuster(AdjusterInterface pValueAdjuster) {
        this.pValueAdjuster = pValueAdjuster;
        return this;
    }

    public MITHrIL matrixFactory(MatrixFactoryInterface<?> matrixFactory) {
        this.matrixFactory = matrixFactory;
        return this;
    }

    public MITHrIL medianAlgorithmFactory(Supplier<StreamMedianComputationInterface> medianAlgorithmFactory) {
        this.medianAlgorithmFactory = medianAlgorithmFactory;
        return this;
    }

    public MITHrIL noPValue(boolean noPValue) {
        this.noPValue = noPValue;
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
        var pl = new ProgressLogger(log, 1, TimeUnit.MINUTES, "iterations");
        pl.start("Starting iterations");
        do {
            try (
                    var batch = prepareBatch(lastBatchElement);
                    var batchNodePerturbations = computeBatchPerturbations(batch);
                    var batchNodeAccumulators = batchNodePerturbations.subtract(batch);
                    var batchRawPathwayAccumulators = computeBatchAccumulators(batchNodeAccumulators)
            ) {
                var first = lastBatchElement == 0 ? 1 : 0;
                if (lastBatchElement == 0) {
                    nodePerturbations    = batchNodePerturbations.column(0);
                    nodeAccumulators     = batchNodeAccumulators.column(0);
                    pathwayAccumulators  = batchRawPathwayAccumulators.column(0);
                    pathwayProbabilities = new double[pathwayAccumulators.length];
                    if (!noPValue) {
                        nodePValues                 = new double[nodePerturbations.length];
                        pathwayPValues              = new double[pathwayAccumulators.length];
                        pathwayNetworkProbabilities = new double[pathwayAccumulators.length];
                        Arrays.fill(pathwayProbabilities, -1.0);
                    }
                }
                if (numberOfRepetitions > 0) {
                    batchRawPathwayAccumulators.forEach(
                            MatrixInterface.Direction.ROW, (v, i) -> medians[i].addElements(v, first));
                }
                if (!noPValue) {
                    countPValueEvents(batchNodePerturbations, first, this.nodePerturbations, nodePValues);
                    countPValueEvents(batchRawPathwayAccumulators, first, pathwayAccumulators, pathwayPValues);
                }
                lastBatchElement += batch.columns();
                pl.update(batch.columns());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (lastBatchElement < numberOfRepetitions);
        pl.done();
        log.info("Applying distribution correction");
        applyDistributionCorrection();
        log.info("Computing p-values");
        finalizePValues();
        log.info("Computing impact factors");
        computeImpactFactors();
    }

    /**
     * Get the output of the algorithm.
     *
     * @return the output of the algorithm.
     */
    public MITHrILOutput output() {
        return new MITHrILOutput(
                nodePerturbations, nodeAccumulators, pathwayAccumulators, pathwayCorrectedAccumulators,
                pathwayImpactFactors, nodePValues, nodeAdjustedPValues, pathwayPValues,
                pathwayAdjustedPValues, pathwayProbabilities, pathwayNetworkProbabilities
        );
    }

    //region Utility methods

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

    /**
     * Initializes the internal state of the algorithm. This method is called automatically by the {@link #run()}
     * method.
     */
    private void init() {
        if (repositoryMatrixTransposed == null) {
            repositoryMatrixTransposed = repositoryMatrix.matrix().transpose();
        }
        if (medians == null) {
            var numberOfPathways = repositoryMatrix.id2Index().size();
            medians = new StreamMedianComputationInterface[numberOfPathways];
            for (var i = 0; i < numberOfPathways; i++) {
                medians[i] = medianAlgorithmFactory.get();
                medians[i].numberOfElements(numberOfRepetitions);
            }
        }
    }

    /**
     * This method prepares the batch of data to be used in the next iteration. It returns a matrix where each row is a
     * gene, and each column is the input of a run. The first column of the first batch is the original input. The other
     * columns are permutations of the original input.
     *
     * @param lastBatchElement the last element of the previous batch
     * @return a matrix containing the batch of data
     */
    private MatrixInterface<?> prepareBatch(int lastBatchElement) {
        var id2index = repositoryMatrix.pathwayMatrix().id2Index();
        int batchSize = Math.min(this.batchSize, (numberOfRepetitions + 1) - lastBatchElement);
        var batchData = new double[id2index.size() * batchSize];
        var i = 0;
        for (var j = 0; j < batchSize; j++) {
            var expressions = (lastBatchElement == 0 && j == 0) ? input.expressions() : input.permute(random);
            for (var e : expressions.object2DoubleEntrySet()) {
                i = id2index.getOrDefault(e.getKey(), -1);
                if (i < 0) continue;
                batchData[i * batchSize + j] = e.getDoubleValue();
            }
        }
        return matrixFactory.of(batchData, id2index.size(), batchSize);
    }

    /**
     * Given a batch of data, this method computes the perturbations of the batch. Given a run, the perturbation is
     * computed as pathwayMatrix * run, where pathwayMatrix is computed as (I-W)^-1.
     *
     * @param batch the batch of data
     * @return the perturbations of the batch stored in a matrix, where each row is a gene, and each column is a run.
     */
    private MatrixInterface<?> computeBatchPerturbations(@NotNull MatrixInterface<?> batch) {
        return batch.preMultiply(repositoryMatrix.pathwayMatrix().matrix());
    }

    /**
     * Given a batch perturbations, this method computes the accumulators of the batch. Given a run, the accumulator is
     * computed as repositoryMatrix^T * run, where ^T is the transpose operator. The repositoryMatrix is a matrix where
     * each row is a gene, and each column is a pathway. Position (i,j) is 1 if gene i is in pathway j, 0 otherwise.
     *
     * @param batchPerturbation the perturbations of a batch computed with computeBatchPerturbations
     * @return the accumulators of the batch stored in a matrix, where each row is a pathway, and each column is a run.
     */
    private MatrixInterface<?> computeBatchAccumulators(@NotNull MatrixInterface<?> batchPerturbation) {
        return batchPerturbation.preMultiply(repositoryMatrixTransposed);
    }

    /**
     * Given a set of node identifiers, this method intersects the set with the nodes of the pathway p.
     *
     * @param ids the set of node identifiers
     * @param p   the pathway identifier
     * @return the intersection of the set with the nodes of the pathway p
     */
    private List<String> intersect(@NotNull Set<String> ids, @NotNull String p) {
        return repository.virtualPathway(p)
                         .edges()
                         .stream()
                         .flatMap(e -> Stream.of(e.left(), e.right()))
                         .distinct()
                         .filter(ids::contains)
                         .toList();
    }

    /**
     * Given an array of node identifiers, this method intersects the array with the nodes of the pathway p.
     *
     * @param ids the array of node identifiers
     * @param p   the pathway identifier
     * @return the intersection of the array with the nodes of the pathway p
     */
    private List<String> intersect(@NotNull String[] ids, @NotNull String p) {
        var idSet = new HashSet<String>();
        Collections.addAll(idSet, ids);
        return repository.virtualPathway(p)
                         .edges()
                         .stream()
                         .flatMap(e -> Stream.of(e.left(), e.right()))
                         .distinct()
                         .filter(idSet::contains)
                         .toList();
    }

    /**
     * Given a pathway, this method computes the probability of observing by chance a number of DE genes equal to the
     * number of DE genes observed in the pathway.
     *
     * @param p the pathway index
     * @return the probability
     */
    private double probability(int p) {
        if (pathwayProbabilities[p] < 0) {
            var id = repositoryMatrix.index2Id().get(p);
            var deGenes = input.expressions().keySet();
            pathwayProbabilities[p] = probabilityComputation.computeProbability(
                    input.nodes(), intersect(input.nodes(), id), deGenes, intersect(deGenes, id));
        }
        return pathwayProbabilities[p];
    }

    /**
     * Correct the raw accumulators of this run by subtracting the median of random accumulators. The median is used to
     * be robust to outliers.
     */
    private void applyDistributionCorrection() {
        if (numberOfRepetitions > 0) {
            pathwayCorrectedAccumulators = new double[pathwayAccumulators.length];
            for (var i = 0; i < pathwayAccumulators.length; i++) {
                pathwayCorrectedAccumulators[i] = pathwayAccumulators[i] - medians[i].currentValue();
            }
        } else {
            pathwayCorrectedAccumulators = pathwayAccumulators;
        }
    }

    /**
     * Finalize the p-values by dividing the count of events by the number of repetitions. Then, the p-values are
     * adjusted for multiple hypothesis testing.
     */
    private void finalizePValues() {
        if (!noPValue) {
            finalizePValues(nodePValues);
            finalizePValues(pathwayPValues);
            System.arraycopy(pathwayPValues, 0, pathwayNetworkProbabilities, 0, pathwayPValues.length);
            for (var i = 0; i < pathwayPValues.length; i++) {
                pathwayPValues[i] = pValueCombiner.combine(pathwayPValues[i], probability(i));
            }
            nodeAdjustedPValues    = pValueAdjuster.adjust(nodePValues);
            pathwayAdjustedPValues = pValueAdjuster.adjust(pathwayPValues);
        }
    }

    /**
     * Finalize the p-values by dividing the count of events by the number of repetitions. Then, the p-values are
     * adjusted for multiple hypothesis testing.
     *
     * @param pValues the array containing the counts to update
     */
    private void finalizePValues(double @NotNull [] pValues) {
        var minPValue = 1.0 / (double) numberOfRepetitions / 100.0;
        for (var i = 0; i < pValues.length; i++) {
            pValues[i] /= numberOfRepetitions;
            if (pValues[i] <= minPValue) {
                pValues[i] = minPValue;
            } else if (pValues[i] > 1) { //This should never happen!
                throw new RuntimeException("P-value greater than 1");
            }
        }
    }

    /**
     * Compute the impact factors of the pathways.
     */
    private void computeImpactFactors() {
        pathwayImpactFactors = new double[pathwayAccumulators.length];
        for (var i = 0; i < pathwayAccumulators.length; i++) {
            var pathway = repositoryMatrix.index2Id().get(i);
            var deGenesPathway = intersect(input.expressions().keySet(), pathway);
            var deGenesPathwayCount = deGenesPathway.size();
            var meanDE = deGenesPathway.stream().mapToDouble(input.expressions()::getDouble).average().orElse(0.0);
            if (meanDE * deGenesPathwayCount == 0) {
                pathwayImpactFactors[i] = 0.0;
            } else {
                var pi = Math.log(probability(i)) / LOG_2;
                pathwayImpactFactors[i] = (Math.abs(pathwayAccumulators[i]) / (meanDE * deGenesPathwayCount)) - pi;
            }
        }
    }
    //endregion
}
