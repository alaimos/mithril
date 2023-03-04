package com.alaimos.MITHrIL.api.Commons;

import java.io.InputStream;
import java.util.Properties;

class Versioning {

    private String currentVersion;

    public Versioning() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("version.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            currentVersion = prop.get("version").toString();
        } catch (Throwable e) {
            currentVersion = "unknown";
        }
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

}
