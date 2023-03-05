package com.alaimos.MITHrIL.api.CommandLine;

import com.alaimos.MITHrIL.api.CommandLine.Interfaces.Options;
import org.kohsuke.args4j.Option;

/**
 * Default implementation of getHelp method for Options class
 */
public abstract class AbstractOptions implements Options {

    @Option(name = "-h", aliases = {"--help"}, usage = "Print this message", help = true)
    protected boolean help = false;


    /**
     * Checks if the help has been requested
     *
     * @return help requested?
     */
    @Override
    public boolean getHelp() {
        return help;
    }

}
