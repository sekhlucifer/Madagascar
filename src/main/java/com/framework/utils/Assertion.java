package com.framework.utils;

import org.testng.Assert;
import org.testng.asserts.SoftAssert;

/**
 * Centralised assertion helper that logs each check to both the console
 * and the active ExtentReports test node before delegating to TestNG.
 *
 * <h3>Hard assertions (fail immediately)</h3>
 * <pre>
 *   Assertion.assertTrue(isVisible, "Login button must be visible");
 *   Assertion.assertEquals(actual, expected, "Page title mismatch");
 * </pre>
 *
 * <h3>Soft assertions (collect all failures)</h3>
 * <pre>
 *   SoftAssert sa = Assertion.softAssert();
 *   Assertion.softTrue(sa, condition1, "Check 1");
 *   Assertion.softEquals(sa, actual, expected, "Check 2");
 *   Assertion.assertAll(sa);   // throws if any soft assertion failed
 * </pre>
 */
public final class Assertion {

    private Assertion() {}

    // ── Hard assertions ───────────────────────────────────────────────────

    public static void assertTrue(boolean condition, String message) {
        if (condition) LogUtils.pass(message);
        else           LogUtils.fail(message);
        Assert.assertTrue(condition, message);
    }

    public static void assertFalse(boolean condition, String message) {
        if (!condition) LogUtils.pass(message);
        else            LogUtils.fail(message);
        Assert.assertFalse(condition, message);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        boolean match = expected != null ? expected.equals(actual) : actual == null;
        if (match) LogUtils.pass(message + " | expected=" + expected + " actual=" + actual);
        else       LogUtils.fail(message + " | expected=" + expected + " actual=" + actual);
        Assert.assertEquals(actual, expected, message);
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj != null) LogUtils.pass(message);
        else             LogUtils.fail(message);
        Assert.assertNotNull(obj, message);
    }

    public static void assertNull(Object obj, String message) {
        if (obj == null) LogUtils.pass(message);
        else             LogUtils.fail(message);
        Assert.assertNull(obj, message);
    }

    public static void assertContains(String actual, String expected, String message) {
        boolean ok = actual != null && actual.contains(expected);
        if (ok) LogUtils.pass(message);
        else    LogUtils.fail(message + " | [" + actual + "] does not contain [" + expected + "]");
        Assert.assertTrue(ok, message);
    }

    // ── Soft assertions ───────────────────────────────────────────────────

    /** Returns a new {@link SoftAssert} instance for a group of checks. */
    public static SoftAssert softAssert() {
        return new SoftAssert();
    }

    public static void softTrue(SoftAssert sa, boolean condition, String message) {
        if (condition) LogUtils.pass("[SOFT] " + message);
        else           LogUtils.fail("[SOFT] " + message);
        sa.assertTrue(condition, message);
    }

    public static void softEquals(SoftAssert sa, Object actual, Object expected, String message) {
        boolean match = expected != null ? expected.equals(actual) : actual == null;
        if (match) LogUtils.pass("[SOFT] " + message);
        else       LogUtils.fail("[SOFT] " + message + " | expected=" + expected + " actual=" + actual);
        sa.assertEquals(actual, expected, message);
    }

    /** Triggers a {@link SoftAssert#assertAll()} — throws if any soft check failed. */
    public static void assertAll(SoftAssert sa) {
        sa.assertAll();
    }
}
