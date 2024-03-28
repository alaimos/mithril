package com.alaimos.MITHrIL.app.CommandLine.Options;

import org.kohsuke.args4j.Option;

import java.io.File;

public class ExportMetapathwayOptions extends MetapathwayOptions {
    @Option(name = "-verbose", usage = "shows verbose computational outline.")
    public boolean verbose = false;
    @Option(name = "-no", aliases = "-nodes-output", usage = "output file for nodes list.")
    public File nodesOutput = null;
    @Option(name = "-eo", aliases = "-edges-output", usage = "output file for edges list.")
    public File edgesOutput = null;
    @Option(name = "-mo", aliases = "-edges-map-output", usage = "output file for pathway to edges map.")
    public File edgesMapOutput = null;
}