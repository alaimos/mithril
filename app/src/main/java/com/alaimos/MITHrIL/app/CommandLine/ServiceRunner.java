package com.alaimos.MITHrIL.app.CommandLine;

import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.app.Plugins.PluginManager;
import dnl.utils.text.table.TextTable;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class ServiceRunner implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServiceRunner.class);

    private final String executable;

    private final String[] args;

    private final HashMap<String, ServiceInterface> services = new HashMap<>();

    /**
     * Create the service runner
     *
     * @param args an array of command line arguments
     */
    public ServiceRunner(String executable, String[] args) {
        this.executable = executable;
        this.args = args;
        fillServicesMap();
    }

    /**
     * Gets the list of all available services
     */
    private void fillServicesMap() {
        PluginManager.INSTANCE.getExtensions(ServiceInterface.class).forEach(s -> {
            if (services.containsKey(s.getShortName())) {
                throw new RuntimeException("Duplicated service name: " + s.getShortName());
            }
            services.put(s.getShortName(), s);
        });
    }

    private void printMainUsage(@NotNull PrintStream out) {
        out.println("java -jar " + executable + " [serviceName] arguments...\n\nAvailable services:\n");
        var columns = new String[]{"Name", "Description"};
        var data = services.values().stream().map(s -> new String[]{s.getShortName(), s.getDescription()}).toArray(String[][]::new);
        Arrays.sort(data, Comparator.comparing(o -> o[0]));
        new TextTable(columns, data).printTable(out, 3);
    }

    private void printServiceUsage(@NotNull PrintStream out, String serviceName, @NotNull CmdLineParser parser) {
        out.println("java -jar " + executable + " " + serviceName + " [options...] arguments...");
        parser.printUsage(out);
        out.println();
        out.println("  Example: java -jar " + executable + " " + serviceName + parser.printExample(OptionHandlerFilter.REQUIRED));
    }

    @Override
    public void run() {
        if (args.length == 0) {
            printMainUsage(System.err);
            System.exit(1);
        } else {
            var serviceName = args[0];
            if (!services.containsKey(serviceName)) {
                log.error("Unrecognized service: " + serviceName);
                printMainUsage(System.err);
                System.exit(2);
            } else {
                var others = new String[args.length - 1];
                if (others.length > 0) {
                    System.arraycopy(args, 1, others, 0, others.length);
                }
                var service = services.get(serviceName);
                var parser = new CmdLineParser(service.getOptions());
                try {
                    parser.parseArgument(others);
                    if (service.getOptions().getHelp()) {
                        printServiceUsage(System.err, serviceName, parser);
                        System.exit(1);
                    }
                    service.run();
                    System.exit(0);
                } catch (Throwable e) {
                    log.error("Error while running service " + serviceName, e);
                    printServiceUsage(System.err, serviceName, parser);
                    System.exit(3);
                }
            }
        }
    }
}
