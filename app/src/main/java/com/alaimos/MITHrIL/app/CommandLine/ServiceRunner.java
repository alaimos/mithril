package com.alaimos.MITHrIL.app.CommandLine;

import com.alaimos.MITHrIL.api.CommandLine.InputParametersException;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.Service;
import dnl.utils.text.table.TextTable;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ServiceLoader;

/**
 * This class runs command line services
 *
 * @author Salvatore Alaimo, Ph.D.
 * @version 2.0.0.0
 * @since 05/01/2016
 */
public class ServiceRunner implements Runnable {

    private String executable;

    private String[] args;

    private ServiceLoader<Service> loader;

    private HashMap<String, Service> services = new HashMap<>();

    /**
     * Create the service runner
     *
     * @param args an array of command line arguments
     */
    public ServiceRunner(String executable, String[] args) {
        this.executable = executable;
        this.args = args;
        loader = ServiceLoader.load(Service.class);
        fillServicesMap();
    }

    /**
     * Gets the list of all available services
     */
    private void fillServicesMap() {
        loader.forEach(s -> {
            if (services.containsKey(s.getShortName())) {
                throw new RuntimeException("Duplicated service name: " + s.getShortName());
            }
            services.put(s.getShortName(), s);
        });
    }

    private void printMainUsage(PrintStream out) {
        out.println("java -jar " + executable + " [serviceName] arguments...\n\nAvailable services:\n");
        var columns = new String[]{"Name", "Description"};
        var data = services.values().stream().map(s -> new String[]{s.getShortName(), s.getDescription()}).toArray(String[][]::new);
        Arrays.sort(data, Comparator.comparing(o -> o[0]));
        new TextTable(columns, data).printTable(out, 3);
    }

    private void printServiceUsage(PrintStream out, String serviceName, CmdLineParser parser) {
        out.println("java -jar " + executable + " " + serviceName + " [options...] arguments...");
        parser.printUsage(out);
        out.println();
        out.println("  Example: java -jar " + executable + " " + serviceName + parser.printExample(OptionHandlerFilter.REQUIRED));
    }

    @Override
    public void run() {
        if (args.length == 0) {
            printMainUsage(System.out);
            System.exit(1);
        } else {
            var serviceName = args[0];
            if (!services.containsKey(serviceName)) {
                System.err.println("Unrecognized service: " + serviceName);
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
                        printServiceUsage(System.out, serviceName, parser);
                        System.exit(1);
                    }
                    service.run();
                    System.exit(0);
                } catch (CmdLineException | InputParametersException e) {
                    System.err.println(e.getMessage());
                    printServiceUsage(System.err, serviceName, parser);
                    System.exit(3);
                }
            }
        }
    }
}
