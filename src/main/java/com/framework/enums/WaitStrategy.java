package com.framework.enums;

/**
 * Named wait strategies used by element-interaction helpers in {@link com.framework.pages.BasePage}.
 */
public enum WaitStrategy {
    /** Wait until the element is visible and interactable (default). */
    VISIBLE,
    /** Wait until the element is present in the DOM (even if hidden). */
    PRESENT,
    /** Wait until the element is not present / detached. */
    INVISIBLE,
    /** No explicit wait — use only when element is guaranteed to be ready. */
    NONE
}
