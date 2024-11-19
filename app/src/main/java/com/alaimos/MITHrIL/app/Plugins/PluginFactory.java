package com.alaimos.MITHrIL.app.Plugins;

import com.alaimos.MITHrIL.api.Plugins.PluginContext;
import org.jetbrains.annotations.NotNull;
import org.pf4j.DefaultPluginFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

public class PluginFactory extends DefaultPluginFactory {

    private static final Logger log = LoggerFactory.getLogger(PluginFactory.class);

    @Override
    protected Plugin createInstance(Class<?> pluginClass, @NotNull PluginWrapper pluginWrapper) {
        PluginContext context = new PluginContext(pluginWrapper.getRuntimeMode());
        try {
            Constructor<?> constructor = pluginClass.getConstructor(PluginContext.class);
            return (Plugin) constructor.newInstance(context);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
