package com.framework.utils;

import com.framework.browser.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Static wait helpers that work with the current thread's Playwright
 * {@link Page}.
 * Complements {@link com.framework.pages.BasePage} for cases where a wait is
 * needed
 * outside a Page Object (e.g., in a utility or data-setup class).
 */
public final class WaitUtil {

    private WaitUtil() {
    }

    private static Page page() {
        return BrowserManager.getPage();
    }

    // wait for element
    public static boolean waitForElement(String xpath, int timeoutMs) {
        try {
            page().waitForSelector(xpath,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits until the given XPath is visible, up to {@code timeoutMs} milliseconds.
     */
    public static boolean waitForVisible(String xpath, int timeoutMs) {
        try {
            page().waitForSelector(xpath,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Waits until the element is hidden or detached. */
    public static boolean waitForHidden(String xpath, int timeoutMs) {
        try {
            page().waitForSelector(xpath,
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Polls until the locator satisfies a custom predicate. */
    public static boolean waitUntil(Locator locator, java.util.function.Predicate<Locator> condition, int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.test(locator))
                    return true;
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private static final String DEFAULT_LOADER_XPATH = "(//div[contains(@class,'spinner-container-style')]//div[@class='progress-style'])[last()]"
            + " | (//div[contains(@class,'fs-container-style')]//div[@class='progress-style'])[last()]"
            + " | //div[contains(@id, 'loader') or contains(@id, 'loading')]"
            + " | //div[contains(@id, 'loader') or contains(@id, 'loading')]/span"
            + " | //div[@id='next-page-loader']/div[@class='spinner2']";

    /**
     * Dynamically waits for the loading screen/spinner to appear and then
     * disappear.
     * Uses a short timeout for the appearance check and a longer timeout for
     * disappearance.
     */
    public static void waitForLoadingScreen() {
        waitForLoadingScreen(DEFAULT_LOADER_XPATH, 50);
    }

    /**
     * Dynamically waits for the loading screen/spinner to appear and then
     * disappear.
     * Uses a short timeout for the appearance check and a longer timeout for
     * disappearance.
     */
    public static void waitForLoadingScreen(String xpath, int maxWaitSeconds) {
        try {
            page().waitForSelector(xpath, new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(1500));
            LogUtils.info("Loading screen appeared. Waiting for it to disappear...");
        } catch (Exception e) {
            LogUtils.info("Loading screen did not appear or already disappeared.");
        }

        try {
            page().waitForSelector(xpath, new Page.WaitForSelectorOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(maxWaitSeconds * 1000L));
            LogUtils.info("Loading screen disappeared. Waiting 1 second for stability...");
            page().waitForTimeout(1000);
            LogUtils.info("Continuing execution.");
        } catch (Exception e) {
            LogUtils.warn("Timeout waiting for loading screen to disappear after " + maxWaitSeconds + " seconds.");
        }
    }

    /**
     * Dynamically waits for a loading element containing the specified text to
     * appear and then disappear.
     */
    public static void waitForLoadingText(String text, int maxWaitSeconds) {
        String xpath = "//*[contains(text(), '" + text
                + "') or contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
                + text.toLowerCase() + "')]";
        waitForLoadingScreen(xpath, maxWaitSeconds);
    }

    /**
     * Dynamically waits for a loading element containing the text "Loading" or
     * "loading" to appear and then disappear.
     */
    public static void waitForLoadingText() {
        waitForLoadingText("Loading", 30);
    }

    /** Pauses the current thread for the given number of seconds. */
    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Waits until the network becomes idle (no new network requests for 500ms), up
     * to timeoutMs.
     */
    public static boolean waitForNetworkIdle(int timeoutMs) {
        try {
            page().waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits until the network becomes idle (no new network requests for 500ms) with
     * a default 30s timeout.
     */
    public static boolean waitForNetworkIdle() {
        return waitForNetworkIdle(30000);
    }
    public static void waitForUnviersal(){

        waitForElement("",0);
        waitForVisible("",0);
        waitForHidden("", 0);
//   - public static boolean waitUntil(Locator locator, java.util.function.Predicate<Locator> condition, int timeoutMs)
//   - public static void waitForLoadingScreen()
//   - public static void waitForLoadingScreen(String xpath, int maxWaitSeconds)
//   - public static void waitForLoadingText(String text, int maxWaitSeconds)
//   - public static void waitForLoadingText()
//   - public static void sleep(int seconds)
//   - public static boolean waitForNetworkIdle(int timeoutMs)
//   - public static boolean waitForNetworkIdle()
    }
}
