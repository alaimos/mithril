package com.alaimos.MITHrIL.api.Plugins;

import org.pf4j.RuntimeMode;

/**
 * @author alaimos
 */
public class PluginContext {

    private final RuntimeMode runtimeMode;

    public PluginContext(RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

}
