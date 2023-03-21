package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import org.pf4j.ExtensionPoint;

public interface ExtensionTypeInterface extends ExtensionPoint {

    public String name();

    public String description();

    public Class<? extends ExtensionInterface> classType();

}
