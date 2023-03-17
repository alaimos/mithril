package com.alaimos.MITHrIL.api.CommandLine.Interfaces;

import org.pf4j.ExtensionPoint;

/**
 * A command line service
 */
public interface ServiceInterface extends Runnable, ExtensionPoint {

    String getShortName();

    String getDescription();

    OptionsInterface getOptions();

}
