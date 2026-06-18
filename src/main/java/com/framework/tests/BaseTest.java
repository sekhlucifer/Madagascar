package com.framework.tests;

import com.framework.browser.BrowserManager;
import com.framework.context.TestContext;
import com.framework.reporting.ExtentManager;
import com.framework.reporting.ExtentReport;
import com.framework.utils.LogUtils;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.framework.config.ConfigurationManager.config;

/**
 * Base class for all test classes.
 *
 * <p>
 * Manages the full Playwright lifecycle and integrates with ExtentReports.
 * Extend this class in every test suite:
 *
 * <pre>
 *   public class LoginTests extends BaseTest {
 *       \@Test
 *       public void loginWithValidCredentials() { ... }
 *   }
 * </pre>
 */
public abstract class BaseTest {

    // ─── Suite Lifecycle ──────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        ExtentManager.getInstance(); // initialise ExtentReports once
        LogUtils.info("=== Test Suite Started ===");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentManager.flush();
        LogUtils.info("=== Test Suite Finished ===");
    }

    // ─── Test Lifecycle ───────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    @Parameters({ "browser", "env" })
    public void beforeMethod(
            @Optional("chrome") String browser,
            @Optional("qa") String env,
            ITestResult result) {

        // System property overrides XML parameter
        String resolvedBrowser = System.getProperty("browser", browser);
        if (System.getProperty("env") == null) {
            System.setProperty("env", env);
        }

        LogUtils.info("Starting test: " + result.getMethod().getMethodName()
                + " | browser=" + resolvedBrowser
                + " | env=" + System.getProperty("env"));

        // Playwright setup
        BrowserManager.initPlaywright();
        BrowserManager.initBrowser(resolvedBrowser);
        BrowserManager.initContext();
        BrowserManager.initPage();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        String testMethodName = result.getMethod().getMethodName();

        // Retrieve the video path before closing context
        java.nio.file.Path videoPath = null;
        try {
            if (BrowserManager.getPage() != null && BrowserManager.getPage().video() != null) {
                videoPath = BrowserManager.getPage().video().path();
            }
        } catch (Exception e) {
            LogUtils.debug("Failed to get video path: " + e.getMessage());
        }

        // Capture screenshot as evidence
        captureAndSaveScreenshot(testMethodName);

        // Clean up browser resources
        BrowserManager.closeAll();
        TestContext.clear();
        TestContext.remove();

        // Move/rename video file and log to Extent Report
        if (videoPath != null) {
            try {
                java.nio.file.Path targetDir = java.nio.file.Paths.get("reports", "videos");
                java.nio.file.Files.createDirectories(targetDir);
                java.nio.file.Path targetPath = targetDir.resolve(testMethodName + ".webm");

                // Wait for the file to exist and stop changing (context close finalizes it)
                int retries = 0;
                while (!java.nio.file.Files.exists(videoPath) && retries < 15) {
                    Thread.sleep(200);
                    retries++;
                }

                if (java.nio.file.Files.exists(videoPath)) {
                    java.nio.file.Files.move(videoPath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    LogUtils.info("Video successfully saved to: " + targetPath.toAbsolutePath());
                    try {
                        ExtentReport.getTest().info("Video Evidence: <a href='../videos/" + testMethodName + ".webm' target='_blank'>Click to View Video</a>");
                    } catch (Exception e) {
                        LogUtils.warn("Could not add video link to Extent Report: " + e.getMessage());
                    }
                } else {
                    LogUtils.warn("Video file does not exist at path: " + videoPath);
                }
            } catch (Exception e) {
                LogUtils.warn("Failed to rename or save video file: " + e.getMessage());
            }
        }

        // Flush report
        ExtentManager.flush();

        LogUtils.info("Test finished: " + testMethodName
                + " | status=" + statusLabel(result.getStatus()));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private void captureAndSaveScreenshot(String testName) {
        try {
            if (BrowserManager.getPage() != null) {
                String dir = config().screenshotPath();
                new File(dir).mkdirs();
                byte[] screenshot = BrowserManager.getPage()
                        .screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                                .setFullPage(false)
                                .setAnimations(com.microsoft.playwright.options.ScreenshotAnimations.DISABLED)
                                .setTimeout(3000));
                Files.write(Paths.get(dir + "/" + testName + ".png"), screenshot);
            }
        } catch (Exception e) {
            LogUtils.debug("Screenshot capture failed: " + e.getMessage());
        }
    }

    private static String statusLabel(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            case ITestResult.SKIP -> "SKIP";
            default -> "UNKNOWN";
        };
    }
}
