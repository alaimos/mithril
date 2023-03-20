package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.AbstractOptions;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.SpeciesDatabaseReader;
import dnl.utils.text.table.TextTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all organisms supported by MITHrIL
 */
public class OrganismsService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(OrganismsService.class);

    private OptionsInterface o = new AbstractOptions() {

        /**
         * Checks if the help has been requested
         *
         * @return help requested?
         */
        @Override
        public boolean getHelp() {
            return super.getHelp();
        }
    };

    @Override
    public String getShortName() {
        return "organisms";
    }

    @Override
    public String getDescription() {
        return "Lists all organisms and their characteristics";
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
        SpeciesDatabaseReader speciesDbReader = SpeciesDatabaseReader.INSTANCE;
        try {
            var db = speciesDbReader.read();
            String[] columns = new String[]{"Id", "Name", "Has miRNA-targets interactions?", "Has TF-miRNAs activations?", "Has REACTOME?"};
            String[][] data = db.values().stream().map(s -> new String[]{s.id(), s.name(), ((s.hasMiRNA()) ? "Yes" : "No"), ((s.hasTranscriptionFactors()) ? "Yes" : "No"), ((s.hasReactome()) ? "Yes" : "No")}).toArray(String[][]::new);
            TextTable tt = new TextTable(columns, data);
            tt.printTable();
        } catch (Exception e) {
            log.error("Error while reading species database", e);
        }
    }
}
