package com.framework.enums;

/**
 * Supported deployment environments, matching the folder names under {@code configs/}.
 */
public enum Environment {
    DEV("dev"),
    QA("qa"),
    STAGING("staging"),
    PROD("prod");

    private final String value;

    Environment(String value) { this.value = value; }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    /** Resolves from the {@code -Denv} system property, defaulting to QA. */
    public static Environment current() {
        String env = System.getProperty("env", "qa").toLowerCase();
        for (Environment e : values()) {
            if (e.value.equals(env)) return e;
        }
        return QA;
    }
}
