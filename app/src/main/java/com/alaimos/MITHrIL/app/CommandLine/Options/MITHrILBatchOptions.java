package com.alaimos.MITHrIL.app.CommandLine.Options;

import org.kohsuke.args4j.Option;

import java.io.File;

public class MITHrILBatchOptions extends MITHrILCommonOptions {

    @Option(name = "-i", aliases = "-in", usage = """
                                                  a tab-separated input file where each line contains a node identifier (gene, microRNA, metabolite) and its Log-Fold-Changes for each experiment.
                                                  If the Log-Fold-Change is zero, the gene is assumed as non-differentially expressed.
                                                  Genes are identified by Entrez or ENSEMBL id, microRNA by mature name (miRBase release 21), metabolites or chemicals by KEGG and REACTOME (ChEBI) ids.
                                                  The first line of the input file must contain a tab-separated list of experiment names.""", required = true)
    public File input = null;
    @Option(name = "-o", aliases = "-out", usage = """
                                                   output directory where all results will be stored. If the directory does not exist, it will be created.
                                                   For each experiment, the following files will be created:
                                                   - a main output file, containing a list of pathways and their scores (experiment_name.output.tsv)
                                                   - a endpoint output file (if enabled), containing a list of pathway endpoints and their scores (experiment_name.endpoints.tsv)
                                                   - a perturbation output file (if enabled), containing a list of pathway perturbations and their scores (experiment_name.perturbations.tsv)
                                                   - a binary output file (if enabled), containing the raw output of the analysis (experiment_name.bin)""", required = true)
    public File output = null;
    @Option(name = "-e", aliases = "-endpoints", usage = "enables endpoint output file")
    public boolean endpointOutput = false;
    @Option(name = "-p", aliases = "-perturbations", usage = "enables perturbations output file.")
    public boolean perturbationOutput = false;
    @Option(name = "-bo", aliases = "-binary-out", usage = "enables binary output file.")
    public boolean binaryOutput = false;
    @Option(name = "-threads-batch", usage = "the number of threads used to process batch in parallel.")
    public int batchThreads = 1;

}