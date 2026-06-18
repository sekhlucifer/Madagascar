package com.framework.enums;

/**
 * Supported browser names, matching the values expected by
 * {@link com.framework.browser.BrowserFactory#from(String)}.
 */
public enum BrowserType {
    CHROME("chrome"),
    CHROMIUM("chromium"),
    FIREFOX("firefox"),
    EDGE("edge"),
    WEBKIT("webkit"),
    SAFARI("safari");

    private final String value;

    BrowserType(String value) { this.value = value; }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }
}
