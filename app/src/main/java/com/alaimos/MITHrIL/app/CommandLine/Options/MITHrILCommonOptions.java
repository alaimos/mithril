package com.alaimos.MITHrIL.app.CommandLine.Options;

import org.kohsuke.args4j.Option;

import java.io.File;

public class MITHrILCommonOptions extends MetapathwayOptions {

    @Option(name = "-combiner", usage = "the name of a p-value combination method (see the list of \"combiner\" extensions for possible values).")
    public String pValueCombiner = "stouffer";
    @Option(name = "-adjuster", usage = "the name of a p-value adjustment method (see the list of \"adjuster\" extensions for possible values).")
    public String pValueAdjuster = "benjamini.hochberg";
    @Option(name = "-number-of-iterations", usage = "number of iterations for the p-value computation.")
    public int pValueIterations = 2001;
    @Option(name = "-batch-size", usage = "number of iterations to be performed in a single batch.")
    public int batchSize = 1000;
    @Option(name = "-t", aliases = "-threads", usage = "number of threads used for matrix operations (negative to disable limit; not supported by all matrix math libraries).")
    public int threads = -1;
    @Option(name = "-filter-output", usage = "a file containing a list of pathways to be shown in the output files")
    public File outputFilter = null;
    @Option(name = "-seed", usage = "for experimental reproducibility sets the seed of the random number generator.")
    public Long randomSeed = null;
    @Option(name = "-verbose", usage = "shows verbose computational outline.")
    public boolean verbose = false;
    @Option(name = "-no-p-value", usage = "disables p-value computation.")
    public boolean noPValue = false;
    @Option(name = "-inversion-factory", usage = "the matrix math library used to create and compute the metapathway matrix representation (see the list of \"matrix-math\" extensions for possible values).")
    public String inversionFactory = "default";
    @Option(name = "-multiplication-factory", usage = "the matrix math library used to perform the operations needed for the MITHrIL iteration (see the list of \"matrix-math\" extensions for possible values).")
    public String multiplicationFactory = "default";
    @Option(name = "-enrichment-probability", usage = "the algorithm used to compute the enrichment probability of the pathway (see the list of \"enrichment-probability\" extensions for possible values).")
    public String enrichmentProbability = "default";
    @Option(name = "-median-algorithm", usage = "the algorithm used to compute the median of a data stream (see the list of \"stream-median\" extensions for possible values).")
    public String medianAlgorithm = "default";
}