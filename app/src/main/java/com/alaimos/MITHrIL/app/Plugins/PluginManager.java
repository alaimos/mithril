package com.alaimos.MITHrIL.app.Plugins;

import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginFactory;

public class PluginManager extends DefaultPluginManager {

    public static final PluginManager INSTANCE = new PluginManager();

    private PluginManager() {
        super();
    }

    @Override
    protected ExtensionFinder createExtensionFinder() {
        DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
        extensionFinder.addServiceProviderExtensionFinder();
        return extensionFinder;
    }

    @Override
    protected PluginFactory createPluginFactory() {
        return new com.alaimos.MITHrIL.app.Plugins.PluginFactory();
    }

}
