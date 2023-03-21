package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;

public class AdjusterInterfaceType implements ExtensionTypeInterface {
    @Override
    public String name() {
        return "adjuster";
    }

    @Override
    public String description() {
        return "This extension provides implementations of several p-value correction methods";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return AdjusterInterface.class;
    }
}
