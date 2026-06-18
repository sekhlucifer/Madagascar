package com.framework.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.framework.utils.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.framework.config.ConfigurationManager.config;

/**
 * Thread-local manager for the Playwright lifecycle.
 *
 * <p>Each test thread owns its own {@link Playwright}, {@link Browser},
 * {@link BrowserContext}, and {@link Page} instances — making the framework
 * safe for parallel execution without any synchronisation overhead.
 *
 * <pre>
 *   // Typical setup in a @BeforeMethod hook
 *   BrowserManager.initPlaywright();
 *   BrowserManager.initBrowser(config().browser());
 *   BrowserManager.initContext();
 *   BrowserManager.initPage();
 *
 *   // Typical teardown in an @AfterMethod hook
 *   BrowserManager.closeAll();
 * </pre>
 */
public final class BrowserManager {

    private static final ThreadLocal<Playwright>     TL_PW      = new ThreadLocal<>();
    private static final ThreadLocal<Browser>        TL_BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> TL_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page>           TL_PAGE    = new ThreadLocal<>();

    private BrowserManager() {}

    // ── Playwright ────────────────────────────────────────────────────────

    public static void initPlaywright() {
        TL_PW.set(Playwright.create());
    }

    public static Playwright getPlaywright() {
        return TL_PW.get();
    }

    // ── Browser ───────────────────────────────────────────────────────────

    public static void initBrowser(String browserName) {
        if (config().browserPersistent()) {
            LogUtils.info("Using persistent browser context. Skipping standalone browser launch.");
            return;
        }
        Browser browser = BrowserFactory.from(browserName).launch(TL_PW.get());
        TL_BROWSER.set(browser);
    }

    public static Browser getBrowser() {
        return TL_BROWSER.get();
    }

    public static void initContext() {
        if (config().browserPersistent()) {
            String userDataDir = config().browserUserDataDir();
            if (userDataDir == null || userDataDir.isEmpty()) {
                throw new RuntimeException("browser.user.data.dir is required when browser.persistent is true");
            }

            String profileName = config().browserProfileName();

            // Playwright's launchPersistentContext ALWAYS loads the "Default" profile folder inside the given userDataDir.
            // If the user specified a custom profile name (like "Profile 4"), we copy its files into a temporary
            // directory's "Default" folder, so that Playwright opens it with all cookies and settings!
            if (profileName != null && !profileName.isEmpty() && !profileName.equalsIgnoreCase("Default")) {
                String tempUserDataDir = System.getProperty("user.dir") + File.separator + ".automation-profile-cache";
                LogUtils.info("Profile directory '" + profileName + "' specified. Copying its files to temporary 'Default' profile to bypass Playwright limitations: " + tempUserDataDir);
                prepareTemporaryProfile(userDataDir, profileName, tempUserDataDir);
                userDataDir = tempUserDataDir;
            }

            LogUtils.info("Launching persistent browser context using user data dir: " + userDataDir);

            com.microsoft.playwright.BrowserType.LaunchPersistentContextOptions options = 
                new com.microsoft.playwright.BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(config().headless())
                    .setChannel("chrome") // Force Chrome for persistent context
                    .setSlowMo(config().slowMotion())
                    .setViewportSize(null) // Honour --start-maximized
                    .setAcceptDownloads(true)
                    .setIgnoreHTTPSErrors(true)
                    .setRecordVideoDir(java.nio.file.Paths.get("reports/videos"))
                    .setRecordVideoSize(1920, 1080);

            java.util.List<String> args = new java.util.ArrayList<>(java.util.Arrays.asList(
                "--ignore-certificate-errors",
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--start-maximized"
            ));
            if (profileName != null && !profileName.isEmpty()) {
                args.add("--profile-directory=" + profileName);
            }
            options.setArgs(args);

            BrowserContext ctx = TL_PW.get().chromium().launchPersistentContext(java.nio.file.Paths.get(userDataDir), options);
            TL_CONTEXT.set(ctx);
        } else {
            Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setViewportSize(null)          // honour --start-maximized
                .setScreenSize(1920, 1080)
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true)
                .setRecordVideoDir(java.nio.file.Paths.get("reports/videos"))
                .setRecordVideoSize(1920, 1080);

            if (config().browserStorageStateEnabled()) {
                String statePath = config().browserStorageStatePath();
                if (statePath != null && !statePath.isEmpty() && new java.io.File(statePath).exists()) {
                    LogUtils.info("Loading browser storage state from: " + statePath);
                    options.setStorageStatePath(java.nio.file.Paths.get(statePath));
                } else {
                    LogUtils.warn("Storage state file does not exist: " + statePath + ". Running fresh session.");
                }
            }

            BrowserContext ctx = TL_BROWSER.get().newContext(options);
            TL_CONTEXT.set(ctx);
        }
    }

    public static BrowserContext getContext() {
        return TL_CONTEXT.get();
    }

    // ── Page ──────────────────────────────────────────────────────────────

    public static void initPage() {
        Page page;
        if (config().browserPersistent() && !TL_CONTEXT.get().pages().isEmpty()) {
            page = TL_CONTEXT.get().pages().get(0);
        } else {
            page = TL_CONTEXT.get().newPage();
        }
        page.setDefaultTimeout(config().timeout());
        TL_PAGE.set(page);
    }

    public static Page getPage() {
        return TL_PAGE.get();
    }

    public static void setPage(Page page) {
        TL_PAGE.set(page);
    }

    // ── Teardown ──────────────────────────────────────────────────────────

    /**
     * Closes all resources owned by the current thread in the correct order:
     * Page → Context → Browser → Playwright.
     */
    public static void closeAll() {
        closeSafely(TL_PAGE.get(),    Page::close,           TL_PAGE);
        closeSafely(TL_CONTEXT.get(), BrowserContext::close, TL_CONTEXT);
        closeSafely(TL_BROWSER.get(), Browser::close,        TL_BROWSER);
        closeSafely(TL_PW.get(),      Playwright::close,     TL_PW);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface Closer<T> { void close(T obj); }

    private static <T> void closeSafely(T obj, Closer<T> closer, ThreadLocal<T> tl) {
        if (obj != null) {
            try { closer.close(obj); } catch (Exception ignored) {}
            tl.remove();
        }
    }

    private static boolean copyFailed = false;
    private static List<String> failedFiles = new ArrayList<>();

    private static void prepareTemporaryProfile(String sourceUserDataDir, String profileName, String tempUserDataDir) {
        copyFailed = false;
        failedFiles.clear();
        try {
            File tempProfileDir = new File(tempUserDataDir, profileName);
            if (!tempProfileDir.exists()) {
                tempProfileDir.mkdirs();
            }

            // Copy Local State (which contains cookie decryption keys) to the temp root
            File srcLocalState = new File(sourceUserDataDir, "Local State");
            File destLocalState = new File(tempUserDataDir, "Local State");
            if (srcLocalState.exists()) {
                try {
                    Files.copy(srcLocalState.toPath(), destLocalState.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LogUtils.info("Copied Local State successfully to decrypt cookies.");
                } catch (IOException e) {
                    LogUtils.warn("Failed to copy Local State file: " + e.getMessage());
                }
            }

            File sourceProfileDir = new File(sourceUserDataDir, profileName);
            if (!sourceProfileDir.exists()) {
                throw new RuntimeException("Source profile directory does not exist: " + sourceProfileDir.getAbsolutePath());
            }

            LogUtils.info("Copying session files from: " + sourceProfileDir.getAbsolutePath() + " to: " + tempProfileDir.getAbsolutePath());

            // List of essential files and directories for session state to copy
            String[] essentials = {
                "Preferences",
                "Cookies",
                "Network",
                "Local Storage",
                "Session Storage"
            };

            for (String name : essentials) {
                File srcFile = new File(sourceProfileDir, name);
                File destFile = new File(tempProfileDir, name);
                if (srcFile.exists()) {
                    copyRecursive(srcFile, destFile);
                }
            }

            if (copyFailed) {
                LogUtils.error("CRITICAL: Failed to copy essential session files (they are locked by an active Chrome process): " + failedFiles);
                throw new RuntimeException("CRITICAL ERROR: Could not copy Chrome profile files because they are currently locked! " +
                    "PLEASE CLOSE ALL GOOGLE CHROME INSTANCES AND BACKGROUND PROCESSES (using Task Manager) before running tests.");
            }

            LogUtils.info("Session files copied successfully!");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy persistent Chrome profile: " + e.getMessage(), e);
        }
    }

    private static void copyRecursive(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    copyRecursive(new File(source, file), new File(destination, file));
                }
            }
        } else {
            try {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                String name = source.getName();
                boolean isEssential = name.equalsIgnoreCase("Cookies") || name.equalsIgnoreCase("Preferences") || source.getAbsolutePath().contains("Local Storage") || source.getAbsolutePath().contains("Session Storage");
                
                if (isEssential && !destination.exists()) {
                    copyFailed = true;
                    failedFiles.add(source.getAbsolutePath());
                }
                LogUtils.warn("Skip/failed locked file: " + source.getAbsolutePath() + " (destination exists: " + destination.exists() + ") -> " + e.getMessage());
            }
        }
    }

    private static String resolveEnvVars(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Resolve %VAR% (Windows style environment variables)
        java.util.regex.Pattern winPattern = java.util.regex.Pattern.compile("%([^%]+)%");
        java.util.regex.Matcher winMatcher = winPattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (winMatcher.find()) {
            String varName = winMatcher.group(1);
            String varValue = System.getenv(varName);
            if (varValue == null) {
                varValue = System.getProperty(varName);
            }
            if (varValue != null) {
                winMatcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(varValue));
            } else {
                winMatcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(winMatcher.group(0)));
            }
        }
        winMatcher.appendTail(sb);

        String resolved = sb.toString();
        // Resolve ${VAR} (Unix style environment variables)
        java.util.regex.Pattern unixPattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher unixMatcher = unixPattern.matcher(resolved);
        StringBuilder sb2 = new StringBuilder();
        while (unixMatcher.find()) {
            String varName = unixMatcher.group(1);
            String varValue = System.getenv(varName);
            if (varValue == null) {
                varValue = System.getProperty(varName);
            }
            if (varValue != null) {
                unixMatcher.appendReplacement(sb2, java.util.regex.Matcher.quoteReplacement(varValue));
            } else {
                unixMatcher.appendReplacement(sb2, java.util.regex.Matcher.quoteReplacement(unixMatcher.group(0)));
            }
        }
        unixMatcher.appendTail(sb2);
        return sb2.toString();
    }
}
