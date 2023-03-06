package com.alaimos.MITHrIL.FastCPUMath;

import com.alaimos.MITHrIL.api.Plugins.AbstractPlugin;
import com.alaimos.MITHrIL.api.Plugins.PluginContext;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.RuntimeMode;

public class FastCPUMathPlugin extends AbstractPlugin {
    public FastCPUMathPlugin(PluginContext context) {
        super(context);
    }

    @Override
    public void start() {
        log.info("FastCPUMathPlugin.start()");
        if (RuntimeMode.DEVELOPMENT.equals(context.getRuntimeMode())) {
            log.info(StringUtils.upperCase("FastCPUMathPlugin"));
        }
    }

    @Override
    public void stop() {
        log.info("FastCPUMathPlugin.stop()");
    }

}
