package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Enrichment.EnrichmentProbabilityComputationInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.BinaryWriter;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;
import com.alaimos.MITHrIL.api.Math.StreamMedian.StreamMedianComputationInterface;
import com.alaimos.MITHrIL.app.Algorithms.MITHrIL;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MatrixBuilderFromMetapathway;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MetapathwayBuilderFromOptions;
import com.alaimos.MITHrIL.app.CommandLine.Options.MITHrILBatchOptions;
import com.alaimos.MITHrIL.app.Data.Readers.MITHrIL.ExpressionBatchReader;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import com.alaimos.MITHrIL.app.Data.Records.MITHrILOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Data.Writers.MITHrIL.MITHrILPathwayOutputWriter;
import com.alaimos.MITHrIL.app.Data.Writers.MITHrIL.MITHrILPerturbationDetailedOutputWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

public class MITHrILBatchService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(MITHrILBatchService.class);

    protected MITHrILBatchOptions options = new MITHrILBatchOptions();

    @Override
    public String getShortName() {
        return "mithril-batch";
    }

    @Override
    public String getDescription() {
        return "runs the MITHrIL algorithm on multiple samples";
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
            final Supplier<Random> randomGenerator = () -> new Random(random.nextLong());
            var extManager = ExtensionManager.INSTANCE;
            var pValueCombiner = extManager.getExtension(CombinerInterface.class, options.pValueCombiner);
            var pValueAdjuster = extManager.getExtension(AdjusterInterface.class, options.pValueAdjuster);
            var probabilityComputation = extManager.getExtension(
                    EnrichmentProbabilityComputationInterface.class, options.enrichmentProbability
            );
            var medianAlgorithmFactory = extManager.getExtensionSupplier(
                    StreamMedianComputationInterface.class, options.medianAlgorithm
            );
            var metapathwayRepository = MetapathwayBuilderFromOptions.build(options, random);
            var inversionMatrixFactory = matrixFactory(options.inversionFactory);
            var metapathwayMatrix = MatrixBuilderFromMetapathway.build(metapathwayRepository, inversionMatrixFactory);
            var multiplicationMatrixFactory = matrixFactory(options.multiplicationFactory);
            log.info("Reading input file");
            var input = readInputFile();
            log.info("Starting MITHrIL on {} samples (Thread pool size: {})", input.size(), options.batchThreads);
            try (var pool = new ForkJoinPool(options.batchThreads)) {
                pool.submit(() -> {
                    input.entrySet().parallelStream().forEach((inputEntry) -> {
                        var threadLogger = LoggerFactory.getLogger(
                                "MITHrIL-Pool-Thread-" + Thread.currentThread().threadId()
                        );
                        var experimentName = inputEntry.getKey();
                        threadLogger.info("Running MITHrIL on {}", experimentName);
                        var inputExpressionObject = inputEntry.getValue();
                        try (var mithril = new MITHrIL()) {
                            mithril.input(inputExpressionObject)
                                   .repository(metapathwayRepository)
                                   .repositoryMatrix(metapathwayMatrix)
                                   .matrixFactory(multiplicationMatrixFactory)
                                   .random(randomGenerator.get())
                                   .batchSize(options.batchSize)
                                   .numberOfRepetitions(options.pValueIterations)
                                   .pValueCombiner(pValueCombiner)
                                   .pValueAdjuster(pValueAdjuster)
                                   .probabilityComputation(probabilityComputation)
                                   .medianAlgorithmFactory(medianAlgorithmFactory)
                                   .noPValue(options.noPValue)
                                   .run();
                            threadLogger.info("Saving output for {}", experimentName);
                            saveOutput(experimentName, mithril.output(), metapathwayRepository, metapathwayMatrix);
                            threadLogger.info("Completed MITHrIL on {}", experimentName);
                        } catch (IOException e) {
                            threadLogger.error("An error occurred on " + experimentName, e);
                        }
                    });
                }).get();
            }
            log.info("MIThrIL completed on all samples");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error("An error occurred", e);
        }
    }

    private void validateOutputDirectory() {
        var f = options.output;
        if (f == null) {
            throw new IllegalArgumentException("Output directory is not specified");
        }
        if (!f.exists() && !f.mkdirs()) {
            throw new IllegalArgumentException("Cannot create output directory");
        }
        if (!f.isDirectory()) {
            throw new IllegalArgumentException("Output path is not a directory");
        }
        if (!f.canWrite()) {
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
        validateOutputDirectory();
    }

    private void saveOutput(
            String experimentName, MITHrILOutput output, Repository metapathwayRepository,
            RepositoryMatrix metapathwayMatrix
    ) throws IOException {
        var outputFile = new File(options.output, experimentName + ".output.txt");
        new MITHrILPathwayOutputWriter(metapathwayRepository, metapathwayMatrix).write(outputFile, output);
        if (options.perturbationOutput) {
            outputFile = new File(options.output, experimentName + ".perturbations.txt");
            new MITHrILPerturbationDetailedOutputWriter(metapathwayRepository, metapathwayMatrix, false)
                    .write(outputFile, output);
        }
        if (options.endpointOutput) {
            outputFile = new File(options.output, experimentName + ".endpoints.txt");
            new MITHrILPerturbationDetailedOutputWriter(metapathwayRepository, metapathwayMatrix, true)
                    .write(outputFile, output);
        }
        if (options.binaryOutput) {
            outputFile = new File(options.output, experimentName + ".bin");
            output.setPathwayIndex2Id(metapathwayMatrix.index2Id());
            output.setNodeIndex2Id(metapathwayMatrix.pathwayMatrix().index2Id());
            new BinaryWriter<MITHrILOutput>().write(outputFile, output);

        }
    }

    private Map<String, ExpressionInput> readInputFile() throws IOException {
        return new ExpressionBatchReader().read(options.input);
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
