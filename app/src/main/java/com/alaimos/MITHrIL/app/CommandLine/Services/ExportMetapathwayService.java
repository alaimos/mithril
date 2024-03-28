package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.app.Algorithms.Metapathway.MetapathwayBuilderFromOptions;
import com.alaimos.MITHrIL.app.CommandLine.Options.ExportMetapathwayOptions;
import com.alaimos.MITHrIL.app.Data.Writers.Metapathway.EdgesIndexWriter;
import com.alaimos.MITHrIL.app.Data.Writers.Metapathway.NodesIndexWriter;
import com.alaimos.MITHrIL.app.Data.Writers.Metapathway.PathwayToEdgesIndexWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

public class ExportMetapathwayService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(ExportMetapathwayService.class);

    protected ExportMetapathwayOptions options = new ExportMetapathwayOptions();

    @Override
    public String getShortName() {
        return "export-metapathway";
    }

    @Override
    public String getDescription() {
        return "exports the metapathway to text files";
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
            var random = new Random();
            var extManager = ExtensionManager.INSTANCE;
            var metapathwayRepository = MetapathwayBuilderFromOptions.build(options, random);
            var metapathway = metapathwayRepository.get();
            if (options.nodesOutput != null) {
                log.info("Exporting nodes list");
                new NodesIndexWriter().write(options.nodesOutput, metapathway);
                log.info("Nodes list exported");
            }
            if (options.edgesOutput != null) {
                log.info("Exporting edges list");
                new EdgesIndexWriter().write(options.edgesOutput, metapathway);
                log.info("Edges list exported");
            }
            if (options.edgesMapOutput != null) {
                log.info("Exporting edges to pathways map");
                new PathwayToEdgesIndexWriter().write(options.edgesMapOutput, metapathwayRepository.virtualPathways());
                log.info("Edges to pathways map exported");
            }
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
        if (options.nodesOutput != null) {
            validateOutputDirectory(options.nodesOutput, "Nodes");
        }
        if (options.edgesOutput != null) {
            validateOutputDirectory(options.edgesOutput, "Edges");
        }
        if (options.edgesMapOutput != null) {
            validateOutputDirectory(options.edgesMapOutput, "Edges map");
        }
    }

}
