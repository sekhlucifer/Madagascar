package com.framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.framework.utils.LogUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.framework.config.ConfigurationManager.config;

/**
 * Singleton manager for the {@link ExtentReports} instance.
 *
 * <p>
 * Creates a time-stamped HTML report under the configured report path.
 * Call {@link #flush()} at the end of the suite to write the report to disk.
 */
public final class ExtentManager {

    private static volatile ExtentReports instance;
    private static String reportFilePath;

    private ExtentManager() {
    }

    /** Returns the path of the generated HTML report file. */
    public static String getReportFilePath() {
        return reportFilePath;
    }

    /** Returns the singleton {@link ExtentReports}, creating it on first call. */
    public static ExtentReports getInstance() {
        return getInstance(null);
    }

    /**
     * Returns the singleton {@link ExtentReports}, creating it on first call with
     * suite name.
     */
    public static ExtentReports getInstance(String suiteName) {
        if (instance == null) {
            synchronized (ExtentManager.class) {
                if (instance == null) {
                    instance = createInstance(suiteName);
                }
            }
        }
        return instance;
    }

    /** Flushes all pending data to the HTML report file. */
    public static void flush() {
        if (instance != null) {
            instance.flush();
        }
    }

    // ─────────────────────────────────────────────────────────────────────

    private static ExtentReports createInstance(String suiteName) {
        String reportDir = config().reportPath();
        new File(reportDir).mkdirs();

        String env = System.getProperty("env", "qa").toUpperCase();
        String browser = System.getProperty("browser", "chrome").toUpperCase();

        String cleanedSuite = "Automation_Report";
        if (suiteName != null && !suiteName.trim().isEmpty()) {
            cleanedSuite = suiteName.replaceAll("[^a-zA-Z0-9_-]", "_")
                    .replaceAll("_+", "_")
                    .replaceAll("^_+|_+$", "");
            if (cleanedSuite.isEmpty()) {
                cleanedSuite = "Automation_Report";
            }
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        reportFilePath = reportDir + "/" + env + "_" + browser + "_" + cleanedSuite + "_" + timestamp + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportFilePath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle(cleanedSuite.replace("_", " ") + " Execution Report");
        spark.config().setReportName(cleanedSuite.replace("_", " ") + " - Test Execution");
        spark.config().setEncoding("UTF-8");

        ExtentReports er = new ExtentReports();
        er.attachReporter(spark);
        er.setSystemInfo("Environment", env);
        er.setSystemInfo("Browser", browser);
        er.setSystemInfo("OS", System.getProperty("os.name"));
        er.setSystemInfo("Java", System.getProperty("java.version"));

        LogUtils.info("Extent report initialised: " + reportFilePath);
        return er;
    }
}
