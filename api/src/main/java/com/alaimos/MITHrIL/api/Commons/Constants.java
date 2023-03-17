package com.alaimos.MITHrIL.api.Commons;

import java.io.InputStream;
import java.util.Properties;

public final class Constants {

    public static final Constants INSTANCE = new Constants();

    private final Properties properties = new Properties();

    private Constants() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("constants.properties")) {
            properties.load(input);
        } catch (Throwable ignored) {
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

}
