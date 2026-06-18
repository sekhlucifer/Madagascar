package com.framework.utils;

import com.framework.reporting.ExtentReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Central logging utility that writes to both Log4j and the current
 * ExtentReports test node simultaneously.
 *
 * <p>Falls back gracefully when no ExtentTest is active (e.g. during setup).
 */
public final class LogUtils {

    private static final Logger LOGGER = LogManager.getLogger(LogUtils.class);

    private LogUtils() {}

    // ── Console + ExtentReports ───────────────────────────────────────────

    public static void info(String message) {
        LOGGER.info(message);
        safeExtent(() -> ExtentReport.getTest().info(message));
    }

    public static void pass(String message) {
        LOGGER.info("[PASS] " + message);
        safeExtent(() -> ExtentReport.getTest().pass(message));
    }

    public static void fail(String message) {
        LOGGER.error("[FAIL] " + message);
        safeExtent(() -> ExtentReport.getTest().fail(message));
    }

    public static void warn(String message) {
        LOGGER.warn("[WARN] " + message);
        safeExtent(() -> ExtentReport.getTest().warning(message));
    }

    public static void skip(String message) {
        LOGGER.info("[SKIP] " + message);
        safeExtent(() -> ExtentReport.getTest().skip(message));
    }

    public static void error(String message) {
        LOGGER.error(message);
        safeExtent(() -> ExtentReport.getTest().fail(message));
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
        safeExtent(() -> ExtentReport.getTest().fail(message + "\n" + t));
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    // ─────────────────────────────────────────────────────────────────────

    private static void safeExtent(Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException ignored) {
            // ExtentTest not yet initialised — log only to console
        } catch (Exception ignored) {}
    }
}
