package com.alaimos.MITHrIL.api.CommandLine.Extensions.Types;

import com.alaimos.MITHrIL.api.CommandLine.Extensions.ExtensionInterface;
import com.alaimos.MITHrIL.api.Math.MatrixFactoryInterface;

public class MatrixFactoryInterfaceType implements ExtensionTypeInterface {
    @Override
    public String name() {
        return "matrix-math";
    }

    @Override
    public String description() {
        return "This extension provides matrix implementations for MITHrIL and other algorithms";
    }

    @Override
    public Class<? extends ExtensionInterface> classType() {
        return MatrixFactoryInterface.class;
    }
}
