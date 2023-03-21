package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.AbstractOptions;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Data.Reader.Pathways.SpeciesDatabaseReader;
import dnl.utils.text.table.TextTable;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplayCategoriesService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(DisplayCategoriesService.class);

    protected DisplayCategoriesOptions options = new DisplayCategoriesOptions();

    @Override
    public String getShortName() {
        return "pathway-categories";
    }

    @Override
    public String getDescription() {
        return "shows all available pathway categories for a species";
    }

    @Override
    public OptionsInterface getOptions() {
        return options;
    }

    @Override
    public void run() {
        try {
            log.info("Reading species database");
            var org = options.organism;
            var species = SpeciesDatabaseReader.INSTANCE.read();
            if (!species.containsKey(org)) {
                log.error("Invalid species: {} not found.", org);
                return;
            }
            var s = species.get(org);
            log.info("Reading pathways for {} into the repository", s.name());
            var repository = s.repository();
            if (options.reactome && s.hasReactome()) {
                log.info("Adding Reactome pathways to the repository");
                s.addReactomeToRepository(repository);
            }
            log.debug("The repository contains {} pathways", repository.size());
            log.info("Repository ready");
            var categories = repository.getCategories();
            String[] columns = new String[]{"Category"};
            String[][] data = categories.stream().map(c -> new String[]{c}).toArray(String[][]::new);
            TextTable tt = new TextTable(columns, data);
            tt.printTable();
        } catch (Throwable e) {
            log.error("An error occurred while displaying pathway categories", e);
        }
    }

    private static class DisplayCategoriesOptions extends AbstractOptions {

        @Option(name = "-o", aliases = "-organism", usage = "the organism used for analysis.\nA list of organisms " +
                "can be obtained by launching the specific utility made available by this software.")
        public String organism = "hsa";

        @Option(name = "-reactome", usage = "add reactome pathways to the internal repository if available for the selected species.")
        public boolean reactome = false;

    }
}
