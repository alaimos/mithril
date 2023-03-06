package com.alaimos.MITHrIL.api.Plugins;

import org.pf4j.Plugin;

public class AbstractPlugin extends Plugin {
    protected final PluginContext context;

    protected AbstractPlugin(PluginContext context) {
        super();
        this.context = context;
    }
}
