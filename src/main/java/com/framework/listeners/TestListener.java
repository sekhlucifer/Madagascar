package com.framework.listeners;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.framework.reporting.ExtentManager;
import com.framework.reporting.ExtentReport;
import com.framework.utils.LogUtils;
import org.testng.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TestNG listener that:
 * <ul>
 * <li>Creates an {@link com.aventstack.extentreports.ExtentTest} node per
 * test.</li>
 * <li>Attaches screenshots to failing tests.</li>
 * <li>Records per-test execution times.</li>
 * <li>Flushes the report at suite and context completion.</li>
 * </ul>
 *
 * <p>
 * Register in {@code testng.xml}:
 * 
 * <pre>
 *   &lt;listeners&gt;
 *     &lt;listener class-name="com.framework.listeners.TestListener"/&gt;
 *   &lt;/listeners&gt;
 * </pre>
 */
public class TestListener implements ITestListener, ISuiteListener {

    // ── Parallel-safe state ───────────────────────────────────────────────
    private static final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private static final Set<String> activeContexts = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean suiteCleared = new AtomicBoolean(false);

    // ── ISuiteListener ────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        if (suiteCleared.compareAndSet(false, true)) {
            startTimes.clear();
            LogUtils.info("Suite started: " + suite.getName());
            ExtentManager.getInstance(suite.getName());
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        LogUtils.info("Suite finished: " + suite.getName());
        ExtentManager.flush();

        // Automatically open the generated Extent report in the default browser
        String reportPath = ExtentManager.getReportFilePath();
        if (reportPath != null) {
            try {
                java.io.File file = new java.io.File(reportPath);
                if (file.exists()) {
                    String absolutePath = file.getAbsolutePath();
                    LogUtils.info("Automatically opening Extent report: " + absolutePath);
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        new ProcessBuilder("cmd", "/c", "start", "", absolutePath).start();
                    } else if (os.contains("mac")) {
                        new ProcessBuilder("open", absolutePath).start();
                    } else {
                        new ProcessBuilder("xdg-open", absolutePath).start();
                    }
                }
            } catch (Exception e) {
                LogUtils.warn("Could not automatically open test report: " + e.getMessage());
            }
        }
    }

    // ── ITestListener ─────────────────────────────────────────────────────

    @Override
    public void onStart(ITestContext ctx) {
        activeContexts.add(ctx.getName());
        LogUtils.info("Context started: " + ctx.getName());
    }

    @Override
    public void onFinish(ITestContext ctx) {
        activeContexts.remove(ctx.getName());
        LogUtils.info("Context finished: " + ctx.getName()
                + " (remaining=" + activeContexts.size() + ")");
        ExtentManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        ExtentReport.createTest(
                name,
                desc != null ? desc : "").assignCategory(result.getMethod().getRealClass().getSimpleName());

        LogUtils.info("► " + name);
        startTimes.put(uniqueKey(result), System.currentTimeMillis());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String name = result.getMethod().getMethodName();
        try {
            ExtentReport.getTest().pass("Test passed",
                    screenshotMedia(name));
        } catch (Exception e) {
            ExtentReport.getTest().pass("Test passed");
        }
        logDuration(result);
        LogUtils.pass("[PASS] " + name);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String name = result.getMethod().getMethodName();
        try {
            ExtentReport.getTest().fail(result.getThrowable(),
                    screenshotMedia(name));
        } catch (Exception e) {
            ExtentReport.getTest().fail(result.getThrowable());
        }
        logDuration(result);

        Throwable t = result.getThrowable();
        if (t != null) {
            LogUtils.error("[FAIL] " + name + " -> " + t.getMessage(), t);
        } else {
            LogUtils.fail("[FAIL] " + name);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String name = result.getMethod().getMethodName();
        ExtentReport.getTest().skip(result.getThrowable() != null
                ? result.getThrowable().getMessage()
                : "Skipped");
        logDuration(result);
        LogUtils.skip("[SKIP] " + name);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        LogUtils.warn("Flaky: " + result.getMethod().getMethodName());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private static com.aventstack.extentreports.model.Media screenshotMedia(String name)
            throws java.io.FileNotFoundException {
        // Capture screenshot right here before looking for the file!
        try {
            if (com.framework.browser.BrowserManager.getPage() != null) {
                String dir = com.framework.config.ConfigurationManager.config().screenshotPath();
                new java.io.File(dir).mkdirs();
                byte[] screenshot = com.framework.browser.BrowserManager.getPage()
                        .screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                                .setFullPage(false)
                                .setTimeout(5000));
                java.nio.file.Files.write(java.nio.file.Paths.get(dir + "/" + name + ".png"), screenshot);
                LogUtils.info("Screenshot successfully captured for report: " + name);
            }
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger(TestListener.class)
                    .warn("Failed to capture screenshot in listener: " + e.getMessage());
        }

        String screenshotPath = com.framework.config.ConfigurationManager.config().screenshotPath() + "/" + name
                + ".png";
        java.io.File file = new java.io.File(screenshotPath);
        if (!file.exists()) {
            throw new java.io.FileNotFoundException("Screenshot file not found: " + screenshotPath);
        }
        return MediaEntityBuilder
                .createScreenCaptureFromPath("../screenshots/" + name + ".png")
                .build();
    }

    private static void logDuration(ITestResult result) {
        String key = uniqueKey(result);
        Long start = startTimes.remove(key);
        if (start == null)
            return;
        double sec = (System.currentTimeMillis() - start) / 1000.0;
        String msg = String.format("Duration: %.2fs — %s", sec, result.getMethod().getMethodName());
        try {
            ExtentReport.getTest().info(msg);
        } catch (Exception ignored) {
        }
        LogUtils.info(msg);
    }

    private static String uniqueKey(ITestResult r) {
        return r.getName() + "_" + System.identityHashCode(r.getInstance()) + "_" + r.hashCode();
    }
}
