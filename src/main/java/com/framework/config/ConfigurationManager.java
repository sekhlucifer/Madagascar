package com.framework.config;

import org.aeonbits.owner.ConfigCache;

/**
 * Singleton accessor for the {@link Configuration} interface.
 * The environment is resolved from the {@code -Denv} system property,
 * defaulting to {@code qa} when not provided.
 *
 * <pre>
 *   // Usage
 *   String url = ConfigurationManager.config().baseUrl();
 * </pre>
 */
public final class ConfigurationManager {

    private static final String DEFAULT_ENV = "dev";

    private ConfigurationManager() {}

    /**
     * Returns the cached {@link Configuration} instance.
     * Sets the {@code env} system property to the default when absent.
     */
    public static Configuration config() {
        if (System.getProperty("env") == null) {
            System.setProperty("env", DEFAULT_ENV);
        }
        return ConfigCache.getOrCreate(Configuration.class);
    }
}
