package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Math.StreamMedian.StreamMedianComputationInterface;

public class StreamMedianComputationInterfaceType implements ExtensionTypeInterface {
    @Override
    public String name() {
        return "stream-median";
    }

    @Override
    public String description() {
        return "The algorithms used to compute the median of a stream of numbers";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return StreamMedianComputationInterface.class;
    }
}
