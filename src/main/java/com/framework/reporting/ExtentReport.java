package com.framework.reporting;

import com.aventstack.extentreports.ExtentTest;

/**
 * Thread-local holder for the current {@link ExtentTest} node.
 *
 * <p>Each test thread creates its own node via {@link #createTest} so
 * parallel execution does not mix log entries across tests.
 */
public final class ExtentReport {

    private static final ThreadLocal<ExtentTest> TL_TEST = new ThreadLocal<>();

    private ExtentReport() {}

    /**
     * Creates a new test node and stores it for the current thread.
     *
     * @param name        test name shown in the report
     * @param description optional description / Jira ID
     */
    public static ExtentTest createTest(String name, String description) {
        ExtentTest test = ExtentManager.getInstance().createTest(name, description);
        TL_TEST.set(test);
        return test;
    }

    /** Creates a test node without a description. */
    public static ExtentTest createTest(String name) {
        return createTest(name, "");
    }

    /**
     * Returns the {@link ExtentTest} node for the current thread.
     *
     * @throws IllegalStateException if called before {@link #createTest}
     */
    public static ExtentTest getTest() {
        ExtentTest test = TL_TEST.get();
        if (test == null) {
            throw new IllegalStateException(
                "ExtentTest not initialised for current thread. Call ExtentReport.createTest() first.");
        }
        return test;
    }

    /** Removes the thread-local entry — call in {@code @AfterMethod}. */
    public static void remove() {
        TL_TEST.remove();
    }
}
