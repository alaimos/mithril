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
import com.alaimos.MITHrIL.app.CommandLine.Options.MITHrILOptions;
import com.alaimos.MITHrIL.app.Data.Readers.MITHrIL.ExpressionMapReader;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import com.alaimos.MITHrIL.app.Data.Records.MITHrILOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import com.alaimos.MITHrIL.app.Data.Writers.MITHrIL.MITHrILPathwayOutputWriter;
import com.alaimos.MITHrIL.app.Data.Writers.MITHrIL.MITHrILPerturbationDetailedOutputWriter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MITHrILService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(MITHrILService.class);

    protected MITHrILOptions options = new MITHrILOptions();

    @Override
    public String getShortName() {
        return "mithril";
    }

    @Override
    public String getDescription() {
        return "runs the MITHrIL algorithm on a single sample";
    }

    @Override
    public OptionsInterface getOptions() {
        return options;
    }

    @Override
    public void run() {
        try {
            if (options.verbose) {
                Configurator.setRootLevel(Level.INFO);
            } else {
                Configurator.setRootLevel(Level.WARN);
            }
            checkInputParameters();
            var random = random();
            var extManager = ExtensionManager.INSTANCE;
            log.info("Reading input file");
            var input = readInputFile();
            var metapathwayRepository = MetapathwayBuilderFromOptions.build(options, random);
            var inversionMatrixFactory = matrixFactory(options.inversionFactory);
            RepositoryMatrix metapathwayMatrix;
            if (options.customizePathwayMatrix) {
                metapathwayMatrix = MatrixBuilderFromMetapathway.build(
                        metapathwayRepository,
                        inversionMatrixFactory,
                        extractCustomizationNodesFromInput(input, metapathwayRepository)
                );
            } else {
                metapathwayMatrix = MatrixBuilderFromMetapathway.build(metapathwayRepository, inversionMatrixFactory);
            }
            var multiplicationMatrixFactory = matrixFactory(options.multiplicationFactory);
            log.info("Running MITHrIL");
            try (var mithril = new MITHrIL()) {
                mithril.input(input)
                       .repository(metapathwayRepository)
                       .repositoryMatrix(metapathwayMatrix)
                       .matrixFactory(multiplicationMatrixFactory)
                       .random(random)
                       .batchSize(options.batchSize)
                       .numberOfRepetitions(options.pValueIterations)
                       .pValueCombiner(extManager.getExtension(CombinerInterface.class, options.pValueCombiner))
                       .pValueAdjuster(extManager.getExtension(AdjusterInterface.class, options.pValueAdjuster))
                       .probabilityComputation(
                               extManager.getExtension(
                                       EnrichmentProbabilityComputationInterface.class, options.enrichmentProbability
                               )
                       )
                       .medianAlgorithmFactory(
                               extManager.getExtensionSupplier(
                                       StreamMedianComputationInterface.class, options.medianAlgorithm
                               )
                       )
                       .noPValue(options.noPValue)
                       .run();
                var output = mithril.output();
                if (options.output != null) {
                    new MITHrILPathwayOutputWriter(metapathwayRepository, metapathwayMatrix)
                            .write(options.output, output);
                }
                if (options.perturbationOutput != null) {
                    new MITHrILPerturbationDetailedOutputWriter(
                            metapathwayRepository, metapathwayMatrix, false
                    ).write(options.perturbationOutput, output);
                }
                if (options.endpointOutput != null) {
                    new MITHrILPerturbationDetailedOutputWriter(
                            metapathwayRepository, metapathwayMatrix, true
                    ).write(options.endpointOutput, output);
                }
                saveBinaryOutput(output, metapathwayMatrix);
            }
            log.info("Done");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error("An error occurred", e);
        }
    }

    private void validateOutputDirectory(File f, String name) {
        if (f == null) return;
        var parent = f.getParentFile();
        if (parent == null) parent = new File(".");
        if (!parent.exists()) {
            throw new IllegalArgumentException(name + " directory does not exist");
        }
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException(name + " is not a directory");
        }
        if (!parent.canWrite()) {
            throw new IllegalArgumentException("Cannot write to " + name + " directory");
        }
    }

    private void checkInputParameters() {
        if (options.input == null) {
            throw new IllegalArgumentException("Input file is not specified");
        }
        if (!options.input.exists() || !options.input.canRead()) {
            throw new IllegalArgumentException("Input file does not exist or is not a file");
        }
        if (options.output == null && options.binaryOutput == null) {
            throw new IllegalArgumentException("An output file should be specified (either text or binary)");
        }
        validateOutputDirectory(options.output, "Output");
        validateOutputDirectory(options.endpointOutput, "Endpoint output");
        validateOutputDirectory(options.perturbationOutput, "Perturbation output");
        validateOutputDirectory(options.binaryOutput, "Binary output");
    }

    private ExpressionInput readInputFile() throws IOException {
        return new ExpressionMapReader().read(options.input);
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

    private void saveBinaryOutput(MITHrILOutput output, RepositoryMatrix repositoryMatrix) throws IOException {
        if (options.binaryOutput == null) return;
        log.info("Saving binary output to {}", options.binaryOutput.getAbsolutePath());
        output.setPathwayIndex2Id(repositoryMatrix.index2Id());
        output.setNodeIndex2Id(repositoryMatrix.pathwayMatrix().index2Id());
        new BinaryWriter<MITHrILOutput>().write(options.binaryOutput, output);
        log.info("Binary output saved");
    }

    private @NotNull List<String> extractCustomizationNodesFromInput(
            @NotNull ExpressionInput input,
            @NotNull Repository repository
    ) {
        var output = new ObjectArrayList<String>();
        var metapathway = repository.get().graph();
        for (var entry : input.expressions().object2DoubleEntrySet()) {
            var value = entry.getDoubleValue();
            if (value == 0.0) continue;
            var key = entry.getKey();
            if (!metapathway.hasNode(key)) continue;
            output.add(entry.getKey());
        }
        output.sort((o1, o2) -> {
            var n1Degree = metapathway.outDegree(metapathway.node(o1));
            var n2Degree = metapathway.outDegree(metapathway.node(o2));
            return Integer.compare(n2Degree, n1Degree);
        });
        return output;
    }
}
