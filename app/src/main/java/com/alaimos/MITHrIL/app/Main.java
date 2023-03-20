package com.alaimos.MITHrIL.app;


import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.app.CommandLine.ServiceRunner;
import com.alaimos.MITHrIL.app.Plugins.PluginManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    public static void main(String[] args) {
        try {
            printLogo();
            var pluginManager = PluginManager.INSTANCE;
            pluginManager.loadPlugins();
            pluginManager.startPlugins();
            Runtime.getRuntime().addShutdownHook(new Thread(pluginManager::stopPlugins));
            new ServiceRunner("MITHrIL2.jar", args).run();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (log.isDebugEnabled()) {
                log.debug("Stack trace:", e);
            }
        }

    }

    private static void printLogo() {
        log.info(StringUtils.repeat("#", 80));
        log.info(StringUtils.center("MITHrIL " + Utils.getCurrentVersion(), 80));
        log.info(StringUtils.repeat("#", 80));
    }

}
