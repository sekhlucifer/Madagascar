package com.framework.exceptions;

/**
 * Thrown when an expected element cannot be found on the page within
 * the configured timeout.
 */
public class ElementNotFoundException extends AutomationException {

    public ElementNotFoundException(String selector) {
        super("Element not found: " + selector);
    }

    public ElementNotFoundException(String selector, int timeoutSeconds) {
        super(String.format("Element not found within %ds: %s", timeoutSeconds, selector));
    }
}
