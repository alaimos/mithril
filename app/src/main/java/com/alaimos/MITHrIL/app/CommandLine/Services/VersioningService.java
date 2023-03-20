package com.alaimos.MITHrIL.app.CommandLine.Services;

import com.alaimos.MITHrIL.api.CommandLine.AbstractOptions;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.OptionsInterface;
import com.alaimos.MITHrIL.api.CommandLine.Interfaces.ServiceInterface;
import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteVersionReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Shows the current version of MITHrIL and eventual updates available
 */
public class VersioningService implements ServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(VersioningService.class);


    OptionsInterface o = new AbstractOptions() {

        /**
         * Checks if the help has been requested
         *
         * @return help requested?
         */
        @Override
        public boolean getHelp() {
            return super.getHelp();
        }
    };

    @Override
    public String getShortName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Shows the version of this software and the latest version available";
    }

    @Override
    public OptionsInterface getOptions() {
        return o;
    }

    /**
     * Gets the list of all organisms and prints it
     */
    @Override
    public void run() {
        RemoteVersionReader rv = new RemoteVersionReader();
        try {
            String version = rv.read();
            log.info(StringUtils.repeat("#", 80));
            log.info("Your version is \"{}\"", Utils.getCurrentVersion());
            log.info("Latest version is \"{}\"", version);
            log.info(StringUtils.repeat("#", 80));
        } catch (IOException e) {
            log.error("Unable to read remote version", e);
        }
    }
}
