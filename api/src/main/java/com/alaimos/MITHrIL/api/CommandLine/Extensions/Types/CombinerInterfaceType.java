package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;

public class CombinerInterfaceType implements ExtensionTypeInterface {
    @Override
    public String name() {
        return "combiner";
    }

    @Override
    public String description() {
        return "This extension provides implementations of several p-value combination methods";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return CombinerInterface.class;
    }
}
