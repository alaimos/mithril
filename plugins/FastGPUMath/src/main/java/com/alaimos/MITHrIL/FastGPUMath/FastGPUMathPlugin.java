package com.alaimos.MITHrIL.FastGPUMath;

import com.alaimos.MITHrIL.api.Plugins.AbstractPlugin;
import com.alaimos.MITHrIL.api.Plugins.PluginContext;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.RuntimeMode;

public class FastGPUMathPlugin extends AbstractPlugin {
    public FastGPUMathPlugin(PluginContext context) {
        super(context);
    }

    @Override
    public void start() {
        log.info("FastGPUMathPlugin.start()");
        if (RuntimeMode.DEVELOPMENT.equals(context.getRuntimeMode())) {
            log.info(StringUtils.upperCase("FastGPUMathPlugin"));
        }
    }

    @Override
    public void stop() {
        log.info("FastGPUMathPlugin.stop()");
    }

}
