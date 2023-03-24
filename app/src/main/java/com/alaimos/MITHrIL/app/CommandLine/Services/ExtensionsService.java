package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.AbstractOptions;
import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionManager;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import dnl.utils.text.table.TextTable;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all extensions supported by MITHrIL for a given type
 */
public class ExtensionsService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(ExtensionsService.class);
    private final InternalOptions o = new InternalOptions();

    @Override
    public String getShortName() {
        return "extensions";
    }

    @Override
    public String getDescription() {
        return "Lists all extensions for a given type";
    }

    @Override
    public OptionsInterface getOptions() {
        return o;
    }

    /**
     * Gets the list of all organisms and prints it
     */
    @Override
    public void run() {
        if (o.type == null || o.type.isEmpty()) {
            var extensionTypes = ExtensionManager.INSTANCE.getExtensionTypes();
            String[] columns = new String[]{"Name", "Description"};
            String[][] data = extensionTypes.values()
                                            .stream()
                                            .map(e -> new String[]{e.name(), e.description()})
                                            .toArray(String[][]::new);
            TextTable tt = new TextTable(columns, data);
            tt.printTable();
        } else {
            var extensions = ExtensionManager.INSTANCE.getExtensions(o.type);
            if (extensions == null) {
                log.error("No extensions found for type {}", o.type);
                return;
            }
            String[] columns = new String[]{"Name", "Description"};
            String[][] data = extensions.values()
                                        .stream()
                                        .map(e -> new String[]{e.name(), e.description()})
                                        .toArray(String[][]::new);
            TextTable tt = new TextTable(columns, data);
            tt.printTable();
        }
    }

    private static class InternalOptions extends AbstractOptions {
        @Option(name = "-t", aliases = "-type", usage = "the type to list extensions for. If not specified, it will list all types.", metaVar = "TYPE")
        public String type = null;
    }
}
