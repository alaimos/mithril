package com.alaimos.MITHrIL.api.CommandLine.Interfaces;

import org.pf4j.ExtensionPoint;

/**
 * A command line service
 */
public interface Service extends Runnable, ExtensionPoint {

    String getShortName();

    String getDescription();

    Options getOptions();

}
