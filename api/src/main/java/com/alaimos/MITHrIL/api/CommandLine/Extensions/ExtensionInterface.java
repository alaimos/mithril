package com.alaimos.MITHrIL.api.CommandLine.Extensions;

import org.pf4j.ExtensionPoint;

public interface ExtensionInterface extends ExtensionPoint {

    /**
     * Returns the name of the extension
     *
     * @return the name
     */
    String name();

    /**
     * Returns the description of the extension
     *
     * @return the description
     */
    default String description() {
        return "";
    }
}
