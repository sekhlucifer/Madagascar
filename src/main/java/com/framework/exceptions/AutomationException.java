package com.framework.exceptions;

/**
 * Checked exception for automation-specific failures that should be
 * surfaced with a meaningful message rather than a raw framework exception.
 */
public class AutomationException extends RuntimeException {

    public AutomationException(String message) {
        super(message);
    }

    public AutomationException(String message, Throwable cause) {
        super(message, cause);
    }
}
