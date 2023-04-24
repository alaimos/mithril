package com.alaimos.MITHrIL.app.CommandLine.Options;

import org.kohsuke.args4j.Option;

import java.io.File;

public class PHENSIMOptions extends MetapathwayOptions {

    @Option(name = "-number-of-iterations", usage = "number of iterations for the bootstrapping procedure.")
    public int iterations = 1000;
    @Option(name = "-number-of-simulations", usage = "number of repetitions for the simulation procedure.")
    public int simulations = 1000;
    @Option(name = "-epsilon", usage = "limit of the interval around zero that is used to identify perturbation " +
            "alterations of little importance.\nValues between -epsilon and epsilon will be considered equal to zero.")
    public double epsilon = 0.001;
    @Option(name = "-seed", usage = "for experimental reproducibility sets the seed of the random number generator.")
    public Long randomSeed = null;
    @Option(name = "-verbose", usage = "shows verbose computational outline.")
    public boolean verbose = false;
    @Option(name = "-i", aliases = "-in", usage = "a tab-separated input file where each line contains " +
            "two values: the identifier of a node and its alteration (OVEREXPRESSION, UNDEREXPRESSION).",
            required = true)
    public File input = null;
    @Option(name = "-o", aliases = "-out", usage = "output file.", required = true)
    public File output = null;
    @Option(name = "-output-pathway-matrix", usage = "output file where the matrix of all pathway accumulators will be written")
    public File outputPathwayMatrix = null;
    @Option(name = "-output-nodes-matrix", usage = "output file where the matrix of all nodes accumulators will be written")
    public File outputNodesMatrix = null;
    @Option(name = "-output-sbml", usage = "output file in SBML format")
    public File outputSBML = null;
    @Option(name = "-output-extended-sif", usage = "output file in Extended SIF format")
    public File outputSIF = null;
    @Option(name = "-t", aliases = "-threads", usage = "number of threads used for matrix operations (negative to disable limit; not supported by all matrix math libraries).")
    public int threads = -1;
    @Option(name = "-non-expressed-file", usage = "a file containing a list of non-expressed node ids.", forbids = "-non-expressed-nodes")
    public File nonExpressedFile = null;
    @Option(name = "-remove-nodes-file", usage = "a file containing a list of node ids to remove from pathways")
    public File removeNodesFile = null;
    @Option(name = "-batch-size", usage = "number of iterations to be performed in a single batch.")
    public int batchSize = 1000;
    @Option(name = "-adjuster", usage = "the name of a p-value adjustment method (see the list of \"adjuster\" extensions for possible values).")
    public String pValueAdjuster = "benjamini.hochberg";
    @Option(name = "-inversion-factory", usage = "the matrix math library used to create and compute the metapathway matrix representation (see the list of \"matrix-math\" extensions for possible values).")
    public String inversionFactory = "default";
    @Option(name = "-multiplication-factory", usage = "the matrix math library used to perform the operations needed for the MITHrIL iteration (see the list of \"matrix-math\" extensions for possible values).")
    public String multiplicationFactory = "default";
    @Option(name = "-metapathway-extension-input-file", usage = "a tab-separated file to extend the metapathway with custom nodes and edges.")
    public File metapathwayExtensionInputFile = null;
    @Option(name = "-custom-node-type-input-file", usage = "a tab-separated file to define custom node types.")
    public File customNodeTypeInputFile = null;
    @Option(name = "-custom-edge-type-input-file", usage = "a tab-separated file to define custom edge types.")
    public File customEdgeTypeInputFile = null;
    @Option(name = "-custom-edge-subtype-input-file", usage = "a tab-separated file to define custom edge subtypes.")
    public File customEdgeSubtypeInputFile = null;

}