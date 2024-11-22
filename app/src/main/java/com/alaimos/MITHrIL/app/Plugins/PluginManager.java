package com.alaimos.MITHrIL.app.Plugins;

import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class PluginManager extends DefaultPluginManager {

    public static final PluginManager INSTANCE = new PluginManager();

    private PluginManager() {
        super();
    }

    @Override
    protected List<Path> createPluginsRoot() {
        var base = super.createPluginsRoot();
        if (isDevelopment()) return base;
        try {
            var f = new File(PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            var pluginsAbsolutePath = Paths.get(f.getParent(), "plugins");
            if (pluginsAbsolutePath.toFile().exists()) {
                return Collections.singletonList(pluginsAbsolutePath);
            }
        } catch (URISyntaxException ignore) {
        }
        return base;
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
