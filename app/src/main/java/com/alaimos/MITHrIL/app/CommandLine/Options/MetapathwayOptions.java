package com.alaimos.MITHrIL.app.CommandLine.Options;

import com.alaimos.MITHrIL.api.CommandLine.AbstractOptions;
import com.alaimos.MITHrIL.api.Data.Records.MiRNA.EvidenceType;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.Arrays;

public class MetapathwayOptions extends AbstractOptions {

    @Option(name = "-organism", usage = "the organism used for analysis.")
    public String organism = "hsa";
    @Option(name = "-node-weight-computation-method", usage = "the name of the node weight computation method used for this run (see the list of \"node-weight-computation\" extensions for possible values).")
    public String nodeWeightComputationMethod = "default";
    @Option(name = "-edge-weight-computation-method", usage = "the name of the edge weight computation method used for this run (see the list of \"edge-weight-computation\" extensions for possible values).")
    public String edgeWeightComputationMethod = "default";
    @Option(name = "-decoys", usage = "adds decoy pathways.")
    public boolean decoys = false;
    @Option(name = "-no-mirna-extension", usage = "disable pathway extension with miRNAs.", forbids = "-extension-evidence-type")
    public boolean noExtension = false;
    @Option(name = "-extension-evidence-type", usage = "type of minimal evidence used when extending pathways.", forbids = "-no-mirna-extension", aliases = {
            "-enrichment-evidence-type"
    })
    public EvidenceType extensionEvidenceType = EvidenceType.STRONG;
    @Option(name = "-reactome", usage = "add reactome pathways to the internal repository if available for the selected species.")
    public boolean reactome = false;
    @Option(name = "-m", aliases = "-meta-pathway", usage = "this option is now ignored. It is always true.", hidden = true)
    public boolean metaPathway_ignored = true;
    @Option(name = "-no-complete", usage = "this option is now ignored.", hidden = true)
    public boolean noCompletePathway_ignored = false;
    @Option(name = "-include-pathways", usage = "a file containing the list of pathways used when building the metapathway")
    public File includePathways = null;
    @Option(name = "-exclude-pathways", usage = "a file containing a list of pathways excluded when building the metapathway")
    public File excludePathways = null;
    public String[] includeCategories = null;
    public String[] excludeCategories = new String[]{
            "Endocrine and metabolic disease", "Neurodegenerative disease", "Human Diseases", "Immune disease",
            "Infectious disease: viral", "Infectious disease: parasitic", "Cardiovascular disease"
    };

    @Option(name = "-include-categories", usage = "a list of pathway categories (separated by comma) to use when " +
            "building the metapathway environment.\nOnly pathways contained in one of these categories will be " +
            "included in the computation.", metaVar = "cat1, cat2, ...", depends = "-m")
    public void setIncludeCategories(@NotNull String s) {
        includeCategories = Arrays.stream(s.split(","))
                                  .filter(v -> !v.isEmpty())
                                  .map(String::trim)
                                  .toArray(String[]::new);
    }

    @Option(name = "-exclude-categories", usage = "a list of pathway categories (separated by comma) to exclude when " +
            "building the metapathway environment.\nIf a pathways is contained in one of these categories then it will" +
            "  be excluded from the computation.", metaVar = "cat1, cat2, ...", depends = "-m")
    public void setExcludeCategories(@NotNull String s) {
        excludeCategories = Arrays.stream(s.split(","))
                                  .filter(v -> !v.isEmpty())
                                  .map(String::trim)
                                  .toArray(String[]::new);
    }
}