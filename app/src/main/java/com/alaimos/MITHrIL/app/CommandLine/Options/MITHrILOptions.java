package com.alaimos.MITHrIL.app.CommandLine.Options;

import org.kohsuke.args4j.Option;

import java.io.File;

public class MITHrILOptions extends MITHrILCommonOptions {

    @Option(name = "-i", aliases = "-in", usage = """
                                                  a tab-separated input file where each line contains two values: a node identifier (gene, microRNA, metabolite) and its Log-Fold-Change.
                                                  If the Log-Fold-Change is absent or zero, the gene is assumed as non-differentially expressed.
                                                  Genes are identified by Entrez or ENSEMBL id, microRNA by mature name (miRBase release 21), metabolites or chemicals by KEGG and REACTOME (ChEBI) ids.""", required = true)
    public File input = null;
    @Option(name = "-o", aliases = "-out", usage = "main output file which contains pathway statistics such " +
            "as Impact Factor, Accumulator, p-Value and Adjusted p-Value.")
    public File output = null;
    @Option(name = "-e", aliases = "-endpoints", usage = "output file which contains perturbations, accumulators, and " +
            "p-values computed for each pathway endpoint.")
    public File endpointOutput = null;
    @Option(name = "-p", aliases = "-perturbations", usage = "output file which contains all perturbations, accumulators, and " +
            "p-values computed for each pathway node.")
    public File perturbationOutput = null;
    @Option(name = "-bo", aliases = "-binary-out", usage = "binary output file.")
    public File binaryOutput = null;

}