package com.alaimos.MITHrIL.api.CommandLine.Extensions;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.pf4j.ExtensionPoint;

public interface ExtensionInterface extends ExtensionPoint {

    /**
     * Returns the name of the extension
     *
     * @return the name
     */
    default String name() {
        return Utils.camelCaseToDotCase(getClass().getSimpleName());
    }

    /**
     * Returns the description of the extension
     *
     * @return the description
     */
    default String description() {
        return "";
    }
}
