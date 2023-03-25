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
    @Option(name = "-filter-output", usage = "a file containing a list of pathways to be shown in the output files")
    public File outputFilter = null;
    @Option(name = "-seed", usage = "for experimental reproducibility sets the seed of the random number generator.")
    public Long randomSeed = null;
    @Option(name = "-verbose", usage = "shows verbose computational outline.")
    public boolean verbose = false;
}