package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;
import com.alaimos.MITHrIL.app.Algorithms.MITHrIL;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MatrixBuilderFromMetapathway;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MetapathwayBuilderFromOptions;
import com.alaimos.MITHrIL.app.CommandLine.Options.MITHrILOptions;
import com.alaimos.MITHrIL.app.Data.Reader.ExpressionMapReader;
import com.alaimos.MITHrIL.app.Data.Records.ExpressionInput;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
                Configurator.setLevel(Logger.ROOT_LOGGER_NAME, Level.INFO);
            } else {
                Configurator.setLevel(Logger.ROOT_LOGGER_NAME, Level.WARN);
            }
            checkInputParameters();
            var random = random();
            var extManager = ExtensionManager.INSTANCE;
            var metapathwayRepository = MetapathwayBuilderFromOptions.build(options, random);
            var inversionMatrixFactory = matrixFactory(options.inversionFactory);
            var metapathwayMatrix = MatrixBuilderFromMetapathway.build(metapathwayRepository, inversionMatrixFactory);
            var multiplicationMatrixFactory = matrixFactory(options.multiplicationFactory);
            log.info("Reading input file");
            var input = readInputFile();
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
                        .noPValue(options.noPValue)
                        .run();
                var output = mithril.output();
                // todo: write output
            }
            log.info("Done");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        } catch (Throwable e) {
            log.error("An error occurred", e);
        }
    }

    private void checkInputParameters() {
        if (options.input == null) {
            throw new IllegalArgumentException("Input file is not specified");
        }
        if (!options.input.exists() || !options.input.canRead()) {
            throw new IllegalArgumentException("Input file does not exist or is not a file");
        }
        if (options.output == null) {
            throw new IllegalArgumentException("Output file is not specified");
        }
        if (options.endpointOutput == null) {
            throw new IllegalArgumentException("Endpoint output file is not specified");
        }
        if (options.perturbationOutput == null) {
            throw new IllegalArgumentException("Perturbation output file is not specified");
        }
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
}
