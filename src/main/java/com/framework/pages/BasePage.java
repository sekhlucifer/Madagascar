package com.framework.pages;

import com.aventstack.extentreports.MediaEntityBuilder;
import com.framework.browser.BrowserManager;
import com.framework.reporting.ExtentReport;
import com.framework.utils.LogUtils;
import com.framework.utils.WaitUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.*;

import static com.framework.config.ConfigurationManager.config;

/**
 * Base class for all Page Objects.
 *
 * <p>
 * Provides a rich set of reusable Playwright interactions — waits, clicks,
 * navigation, file downloads, AG-Grid helpers, filter utilities, and assertions
 * —
 * that any application-specific page can inherit without duplication.
 */
public abstract class BasePage {

    // ── Page accessor ──────────────────────────────────────────────────────
    protected Page page() {
        return BrowserManager.getPage();
    }

    public Locator getFirstElement(String xpath) {
        return page().locator(xpath).first();
    }

    public void handleMicrosoftLoginBypass() {
        String currentUrl = page().url();
        LogUtils.info("Current URL before login bypass check: " + currentUrl);

        for (int i = 0; i < 3; i++) {
            currentUrl = page().url();
            LogUtils.info("Bypass Step " + (i + 1) + " - Current URL: " + currentUrl);

            if (!currentUrl.contains("login.microsoftonline.com") && !currentUrl.contains("login.windows.net")) {
                LogUtils.info("Not on Microsoft login page anymore. Exiting bypass flow.");
                break;
            }

            // Small wait to allow MS login page elements to settle
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
            // page().pause();
            if (isVisible(MS_ACCOUNT_TILE_XPATH)) {
                LogUtils.info("Account list detected. Clicking the first signed-in account to log in automatically...");
                click(MS_ACCOUNT_TILE_XPATH);
                waitForPageLoad();
            } else if (isVisible(MS_STAY_SIGNED_IN_SUBMIT_XPATH)) {
                LogUtils.info("Stay signed in? prompt detected. Clicking 'Yes' to complete redirect...");
                click(MS_STAY_SIGNED_IN_SUBMIT_XPATH);
                waitForPageLoad();
            } else if (isVisible(MS_EMAIL_INPUT_XPATH)) {
                String email = config().appUsername();
                LogUtils.info("Empty sign-in box detected. Automatically entering configured email: " + email);
                fill(MS_EMAIL_INPUT_XPATH, email);
                click(MS_NEXT_BUTTON_XPATH);
                waitForPageLoad();
            } else {
                LogUtils.info(
                        "No active Microsoft login elements (account list, email input, or 'Stay signed in' prompt) found.");
            }
        }
    }

    // ── Common XPaths ─────────────────────────────────────────────────────

    // --- Refactored Selectors ---
    private static final String MS_ACCOUNT_TILE_XPATH = "//div[@id='tilesHolder']//div[contains(@class,'tile') or @role='listitem'] | //div[@role='listitem']";
    private static final String MS_EMAIL_INPUT_XPATH = "//input[@type='email']";
    private static final String MS_NEXT_BUTTON_XPATH = "//input[@type='submit' or @value='Next']";
    private static final String MS_STAY_SIGNED_IN_SUBMIT_XPATH = "//*[@id='idSIButton9' or @value='Yes' or @type='submit']";
    private static final String TEXT_PRESENT_XPATH_TEMPLATE = "//*[contains(text(),'%s')]";
    private static final String ERROR_MESSAGE_XPATH_TEMPLATE = "//div[@role='alert']/span[contains(text(),'%1$s')] | //td[contains(text(),'%1$s')]";
    private static final String MANDATORY_FIELD_ERROR_XPATH_TEMPLATE = "//div[@role='alert']/span[contains(text(),'%s')]";
    private static final String DISMISS_FORM_CHANGES_BTN_XPATH = "//button[@type='button' and text()='Discard'] | (//button[@class='ico-error close-button'])[last()] | //button[@type='button' and text()='Close'] | //button[@type='button' and text()='Ok']";
    private static final String GRID_CELL_VALUE_XPATH_TEMPLATE = "//div[@role='row' and @row-index='%d']//div[@role='gridcell' and @col-id='%s']";
    private static final String GRID_ROW_COUNT_XPATH = "//div[contains(@class,'ag-center-cols-container')]//div[@role='row'] | //div[contains(@class,'dataTables_scrollBody')]//tr[@data-row-id and @role='row']";
    private static final String SET_CARD_CONDITION_XPATH = "((//div[@class='filter-accordion-header'])[last()])//div[contains(@class,'selectOuter')]";
    private static final String CARD_CONDITION_OPTION_XPATH = "(//li[@id='1'])[last()]";
    private static final String ADD_FIELD_BTN_XPATH = "//button[contains(@class,'rounded-button') and contains(text(),'Field')][last()]";
    private static final String SELECT_FIELD_BTN_XPATH = "(//div[contains(@class,'add-field-dropdown')]/button)[last()]";
    private static final String LABEL_SEARCH_XPATH = "(//div[@id='search_outer']/input)[last()]";
    private static final String LABEL_OPTION_XPATH_TEMPLATE = "(//li/span[starts-with(.,'%s')]//parent::li)[last()]";
    private static final String VALUE_INPUT1_XPATH_TEMPLATE = "(//form//button/span[contains(text(),'%s')]//ancestor::form//div[@class='filter-searchvalue']//input)[last()]";
    private static final String VALUE_INPUT2_XPATH_TEMPLATE = "(//form//button/span[contains(text(),'%s')]//ancestor::form//div[contains(@class,'filter-searchvalue')]//input)[last()]";
    private static final String QUICK_FILTER_PROFILE_XPATH = "//div[@class='quick-filter-section']//button";
    private static final String QUICK_FILTER_SEARCH_OPTION_XPATH = "//input[@id='searchOption']";
    private static final String QUICK_FILTER_FIELD_XPATH_TEMPLATE = "(//div[select[@name='quickFilter']]//div[contains(text(),'%s')])[1]";
    private static final String QUICK_FILTER_VALUE_INPUT_XPATH = "//input[@placeholder='Type to filter']";
    private static final String PAGE_SIZE_OPTION_XPATH_TEMPLATE = "//ul//li//span[text()='%s']/parent::li";
    private static final String SELECT_ROW_CHECKBOX_XPATH_TEMPLATE = "(//td[contains(@class,'select-checkbox')])[%1$d] | (//div[@data-ref='eCheckbox']//div[@data-ref='eWrapper'])[%1$d]";
    private static final String MODULE_HEADER_XPATH_TEMPLATE = "//h2[contains(@class,'table-flt-header-left-title-text') and contains(text(),'%s')]";
    private static final String AG_COLUMN_HEADER_XPATH_TEMPLATE = "//div[contains(@class,'ag-header')]//div[@role='columnheader']//*[contains(text(),'%s')]";
    private static final String OK_BUTTON_XPATH = "//button[text()='OK'] | //button[text()='Ok']";
    private static final String YES_BUTTON_XPATH = "//button[normalize-space()='Yes']";
    private static final String SAVE_BUTTON_XPATH = "//button[text()='Save']";
    private static final String CANCEL_BUTTON_XPATH = "//button[@type='button' and text()='Cancel']";

    protected static final String LOADER_XPATH = "(//div[contains(@class,'spinner-container-style')]//div[@class='progress-style'])[last()]"
            + " | (//div[contains(@class,'fs-container-style')]//div[@class='progress-style'])[last()]";

    protected static final String IFRAME_SELECTOR = "//iframe[@id='core-frame-content']";

    // ═══════════════════════════════════════════════════════════════════════
    // ── Navigation ────────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public void navigateTo(String url) {
        LogUtils.info("Navigating to: " + url);
        page().navigate(url, new Page.NavigateOptions().setTimeout(60_000));
        waitForPageLoad();
    }

    public void navigateToApp() {
        navigateTo(config().baseUrl() + config().homePath());
    }

    public void goBack() {
        page().goBack();
        waitForPageLoad();
    }

    public void scrollToTop() {
        page().evaluate("window.scrollTo(0, 0)");
    }

    /**
     * Scrolls the given element into the center of the viewport so it is
     * clearly visible in a screenshot.
     */
    public void scrollIntoView(String xpath) {
        page().locator(xpath).first().evaluate("el => el.scrollIntoView({ behavior: 'instant', block: 'center' })");
        page().waitForTimeout(300);
    }

    public void reloadPage() {
        String url = page().url();
        try {
            page().reload();
        } catch (PlaywrightException e) {
            if (e.getMessage() != null && e.getMessage().contains("ERR_NETWORK_CHANGED")) {
                page().navigate(url);
            } else {
                throw e;
            }
        }
        waitForPageLoad();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Page Load Waits ────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public void waitForPageLoad() {
        try {
            page().waitForLoadState(LoadState.DOMCONTENTLOADED);
            page().waitForLoadState(LoadState.LOAD);
            waitForLoadingSpinner();
        } catch (Exception e) {
            LogUtils.warn("Page load state exception (non-fatal): " + e.getMessage());
        }
    }

    public void waitForPageToLoad() {
        try {
            page().waitForLoadState(LoadState.DOMCONTENTLOADED);
            page().waitForLoadState(LoadState.LOAD);
            try {
                page().waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30000));
            } catch (Exception e) {
                LogUtils.warn("Network idle wait timeout (non-fatal): " + e.getMessage());
            }
            waitForLoadingSpinner();
        } catch (Exception e) {
            LogUtils.warn("Page load wait exception (non-fatal): " + e.getMessage());
        }
    }

    public void universalWait() {
        universalWait((String) null);
    }

    public void universalWait(String xpath) {
        try {
            page().waitForLoadState(LoadState.DOMCONTENTLOADED);
            page().waitForLoadState(LoadState.LOAD);
            try {
                page().waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30000));
            } catch (Exception e) {
                LogUtils.warn("Network idle wait timeout (non-fatal): " + e.getMessage());
            }
            waitForLoadingSpinner();

            int retries = 0;
            while (isVisible(LOADER_XPATH) && retries < 20) {
                page().waitForTimeout(500);
                retries++;
            }

            if (xpath != null && !xpath.isEmpty()) {
                waitForElement(xpath, 15);
            }
        } catch (Exception e) {
            LogUtils.warn("Universal wait exception (non-fatal): " + e.getMessage());
        }
    }

    public void universalWait(Locator locator) {
        try {
            page().waitForLoadState(LoadState.DOMCONTENTLOADED);
            page().waitForLoadState(LoadState.LOAD);
            try {
                page().waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30000));
            } catch (Exception e) {
                LogUtils.warn("Network idle wait timeout (non-fatal): " + e.getMessage());
            }
            waitForLoadingSpinner();

            int retries = 0;
            while (isVisible(LOADER_XPATH) && retries < 20) {
                page().waitForTimeout(500);
                retries++;
            }

            if (locator != null) {
                waitForElement(locator, 15);
            }
        } catch (Exception e) {
            LogUtils.warn("Universal wait exception (non-fatal): " + e.getMessage());
        }
    }

    public void waitForLoadingSpinner() {
        try {
            page().locator(LOADER_XPATH).waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(20_000));
        } catch (Exception e) {
            LogUtils.info("Spinner wait exception (non-fatal): " + e.getMessage());
        }
    }

    public static void staticWait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Element Waits ─────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Polls until the element is visible and enabled, or the timeout elapses.
     *
     * @return {@code true} if found within the default 10-second window.
     */
    public boolean waitForElement(String xpath) {
        return waitForElement(xpath, 10);
    }

    public boolean waitForElement(String xpath, int timeoutSeconds) {
        LogUtils.info("Waiting for element: " + xpath);
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < deadline) {
            try {
                Locator loc = page().locator(xpath).first();
                if (loc.isVisible() && loc.isEnabled() && !loc.isHidden())
                    return true;
            } catch (Exception ignored) {
            }
        }
        LogUtils.warn("Element not found within " + timeoutSeconds + "s: " + xpath);
        return false;
    }

    public boolean waitForElement(Locator locator) {
        return waitForElement(locator, 10);
    }

    public boolean waitForElement(Locator locator, int timeoutSeconds) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutSeconds * 1000L));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForElementDetached(String xpath) {
        waitForElementDetached(xpath, 8);
    }

    public void waitForElementDetached(String xpath, int timeoutSeconds) {
        try {
            if (waitForElement(xpath, 3)) {
                page().locator(xpath).waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.DETACHED)
                        .setTimeout(timeoutSeconds * 1000L));
            }
        } catch (Exception ignored) {
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Click Interactions ─────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public boolean isClickable(String xpath) {
        waitForElement(xpath);
        return isEnabled(xpath);
    }

    public boolean isClickable(Locator locator) {
        waitForElement(locator);
        return locator.first().isEnabled();
    }

    public void click(String xpath) {
        waitForElement(xpath);
        page().locator(xpath).first().click();
    }

    public void clickForce(String xpath) {
        waitForElement(xpath);
        page().locator(xpath).first().click(new Locator.ClickOptions().setForce(true));
    }

    public void click(Locator locator) {
        waitForElement(locator);
        locator.first().click();
    }

    public void clickJs(String cssSelector) {
        page().evaluate("el => el.click()",
                page().locator(cssSelector).elementHandle());
    }

    public void hover(String xpath) {
        page().hover(xpath);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Text / Input Interactions ──────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public void fill(String xpath, String value) {
        waitForElement(xpath);
        Locator loc = page().locator(xpath).first();
        loc.clear();
        loc.fill(value);
    }

    public String getValue(String xpath) {
        waitForElement(xpath);
        Locator loc = page().locator(xpath);
        return loc.inputValue();
    }

    public void fill(Locator locator, String value) {
        waitForElement(locator);
        locator.first().clear();
        locator.first().fill(value);
    }

    // ── Clear Field ────────────────────────────────────────────────────────

    /**
     * Clears all text from the input field identified by the given XPath.
     *
     * @param xpath XPath of the input field to clear
     */
    public void clearField(String xpath) {
        waitForElement(xpath);
        Locator loc = page().locator(xpath).first();
        loc.clear();
        LogUtils.info("Cleared field: " + xpath);
    }

    /**
     * Clears all text from the input field identified by the given Locator.
     *
     * @param locator Playwright Locator of the input field to clear
     */
    public void clearField(Locator locator) {
        waitForElement(locator);
        locator.first().clear();
        LogUtils.info("Cleared field via Locator");
    }

    public void typeSlowly(String xpath, String value) {
        waitForElement(xpath);
        Locator loc = page().locator(xpath).first();
        loc.clear();
        for (char c : value.toCharArray()) {
            page().keyboard().type(String.valueOf(c));
            page().waitForTimeout(30);
        }
    }

    public String getText(String xpath) {
        return page().locator(xpath).first().textContent().trim();
    }

    public List<String> getAllTextContents(String xpath) {
        return page().locator(xpath).allTextContents();
    }

    public String getInputValue(String xpath) {
        return page().locator(xpath).first().inputValue();
    }

    public void pressKey(String key) {
        page().keyboard().press(key);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Visibility / State Checks ──────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public boolean isVisible(String xpath) {
        try {
            return page().locator(xpath).first().isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEnabled(String xpath) {
        try {
            return page().locator(xpath).first().isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementPresent(String xpath) {
        try {
            return page().locator(xpath).count() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTextPresentOnPage(String text) {
        String locator = String.format(TEXT_PRESENT_XPATH_TEMPLATE, text);
        waitForElement(locator);
        return isVisible(locator);
    }

    public void captureScreenshot(String name) {
        try {
            String dir = config().screenshotPath();
            new File(dir).mkdirs();
            page().screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(dir + "/" + name + ".png"))
                    .setFullPage(true)
                    .setTimeout(15000));
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger(BasePage.class)
                    .warn("Screenshot capture timed out. Retrying with viewport-only screenshot...");
            try {
                String dir = config().screenshotPath();
                page().screenshot(new Page.ScreenshotOptions()
                        .setPath(Paths.get(dir + "/" + name + ".png"))
                        .setFullPage(false)
                        .setTimeout(5000));
            } catch (Exception ex) {
                org.apache.logging.log4j.LogManager.getLogger(BasePage.class)
                        .warn("Fallback screenshot capture also failed: " + ex.getMessage());
            }
        }
    }

    /**
     * Captures the entire page as a screenshot by first resizing the viewport
     * to 1920x1080 so all content is visible, then restores the original size.
     */
    public void captureFullPageScreenshot(String name) {
        String dir = config().screenshotPath();
        try {
            new File(dir).mkdirs();
            page().screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(dir + "/" + name + ".png"))
                    .setFullPage(false)
                    .setAnimations(com.microsoft.playwright.options.ScreenshotAnimations.DISABLED)
                    .setTimeout(15000));
            LogUtils.info("Screenshot saved: " + dir + "/" + name + ".png");
        } catch (Exception e) {
            LogUtils.debug("Screenshot capture failed: " + e.getMessage());
        }

        // Automatically attach to active Extent Report if available
        try {
            if (ExtentReport.getTest() != null && new File(dir + "/" + name + ".png").exists()) {
                String relativePath = "../screenshots/" + name + ".png";
                ExtentReport.getTest().info(name,
                        MediaEntityBuilder.createScreenCaptureFromPath(relativePath).build());
            }
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger(BasePage.class)
                    .warn("Could not attach screenshot to report: " + e.getMessage());
        }
    }

    public byte[] captureScreenshotBytes() {
        try {
            return page().screenshot(new Page.ScreenshotOptions()
                    .setFullPage(false)
                    .setAnimations(com.microsoft.playwright.options.ScreenshotAnimations.DISABLED)
                    .setTimeout(3000));
        } catch (Exception e) {
            LogUtils.debug("Screenshot bytes capture failed: " + e.getMessage());
            return new byte[0];
        }
    }

    public void attachScreenshotToReport(String methodName) {
        try {
            String dir = config().screenshotPath();
            new File(dir).mkdirs();
            byte[] bytes = captureScreenshotBytes();
            Path path = Paths.get(dir + "/" + methodName + ".png");
            Files.write(path, bytes);
            String relativePath = "screenshots/" + methodName + ".png";
            ExtentReport.getTest().info(methodName,
                    MediaEntityBuilder.createScreenCaptureFromPath(relativePath).build());
        } catch (Exception e) {
            LogUtils.warn("Could not attach screenshot: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Alerts / Messages ─────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    private static final String ALERT_XPATH = "(//div[@role='alert']//span)[2]"
            + " | //th//p[text()='Reason']/ancestor::table//td[count(//p[text()='Reason']/ancestor::th/preceding-sibling::th)+1]"
            + " | (//div[text()='Warning']/parent::div/following-sibling::div)[1]";

    public boolean verifyAlertMessage(String... expectedMessages) {
        waitForPageLoad();
        Locator cell = page().locator(ALERT_XPATH).first();
        cell.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(60_000));
        String actual = cell.innerText();
        captureScreenshot("alert_message");
        LogUtils.info("Alert message: " + actual);
        for (String expected : expectedMessages) {
            if (actual.contains(expected)) {
                LogUtils.pass("Alert verified: " + expected);
                dismissFormChanges();
                return true;
            }
        }
        LogUtils.fail("Alert mismatch. Expected one of: " + Arrays.toString(expectedMessages) + " | Actual: " + actual);
        Assert.fail("Alert not verified. Expected: " + Arrays.toString(expectedMessages));
        return false;
    }

    public void verifyErrorMessage(String message) {
        String xpath = String.format(ERROR_MESSAGE_XPATH_TEMPLATE, message);
        Locator loc = page().locator(xpath);
        loc.waitFor();
        Assert.assertTrue(loc.isVisible(), "Error message not displayed: " + message);
        LogUtils.info("Error message verified: " + message);
    }

    public void verifyMandatoryFieldError(String... fields) {
        String prefix = "Please enter valid value for: ";
        Locator loc = page().locator(String.format(MANDATORY_FIELD_ERROR_XPATH_TEMPLATE, prefix));
        loc.waitFor();
        String actual = loc.textContent();
        String fieldsPart = actual.replace(prefix, "").trim();
        Set<String> actualSet = Arrays.stream(fieldsPart.split(","))
                .map(String::trim).collect(Collectors.toSet());
        for (String f : fields) {
            Assert.assertTrue(actualSet.contains(f.trim()), "Missing field in error: " + f);
        }
    }

    public void dismissFormChanges() {
        try {
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                Locator btn = page().locator(DISMISS_FORM_CHANGES_BTN_XPATH);
                if (btn.isVisible()) {
                    btn.hover();
                    btn.click();
                    waitForLoadingSpinner();
                    waitForPageLoad();
                }
                if (!btn.isVisible())
                    break;
                Thread.sleep(300);
            }
        } catch (Exception ignored) {
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Dropdown Helpers ───────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    private static final String DROPDOWN_BUTTON = "(//div[contains(@name,'%s')]//button)[last()]";
    private static final String SEARCH_OPTION = "//input[@name='searchOption' and @type='search']";
    private static final String DROPDOWN_OPTION = "//ul[contains(@class,'selectDropdown')]//*[contains(text(),'%s')]";

    public void selectFromSearchableDropdown(String fieldName, String value) {
        staticWait(1);
        String btn = String.format(DROPDOWN_BUTTON, fieldName);
        waitForElement(btn);
        page().locator(btn).click();
        staticWait(1);
        page().fill(SEARCH_OPTION, value);
        String option = String.format(DROPDOWN_OPTION, value);
        waitForElement(option);
        page().locator(option).click();
        staticWait(1);
    }

    public void selectFromDropdown(String fieldName, String value) {
        try {
            String btn = String.format(DROPDOWN_BUTTON, fieldName);
            waitForElement(btn);
            page().locator(btn).click();
            staticWait(1);
            String option = String.format(DROPDOWN_OPTION, value);
            Locator optionLoc = page().locator(option);
            if (optionLoc.count() > 0) {
                optionLoc.first().click();
            } else {
                LogUtils.warn("Dropdown option not found: " + value);
            }
        } catch (Exception e) {
            LogUtils.warn("Dropdown selection failed for: " + value);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── File Download ──────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public boolean downloadFile(String triggerXpath) {
        try {
            Download dl = page().waitForDownload(
                    () -> page().locator(triggerXpath).click());
            File dumpDir = new File("dumpDownload");
            dumpDir.mkdirs();
            dl.saveAs(Paths.get(dumpDir.getCanonicalPath(), dl.suggestedFilename()));
            return true;
        } catch (Exception e) {
            LogUtils.warn("File download failed: " + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── AG-Grid Utilities ─────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    private static final String AG_HEADER_VIEWPORT = ".ag-header-viewport";
    private static final String AG_CENTER_VIEWPORT = ".ag-center-cols-viewport";
    private static final String AG_ALL_COL_HEADERS = "//div[@role='columnheader' and not(contains(@class,'ag-column-group'))]";

    /**
     * Returns all visible header texts by scrolling the grid horizontally.
     */
    public List<String> getAllGridHeaders() {
        Set<String> headers = new LinkedHashSet<>();
        scrollGridTo(0);
        int steps = getHorizontalScrollSteps();
        for (int i = 0; i < steps; i++) {
            page().locator(AG_ALL_COL_HEADERS).allTextContents()
                    .stream().map(String::trim).filter(s -> !s.isEmpty())
                    .forEach(headers::add);
            scrollGridStepRight();
        }
        scrollGridTo(0);
        return new ArrayList<>(headers);
    }

    public String getGridCellValue(int rowIndex, String colId) {
        String xpath = String.format(GRID_CELL_VALUE_XPATH_TEMPLATE, rowIndex, colId);
        Locator cell = page().locator(xpath).first();
        return cell.count() > 0 ? cell.textContent().trim() : "";
    }

    public int getGridRowCount() {
        waitForElement(GRID_ROW_COUNT_XPATH);
        return page().locator(GRID_ROW_COUNT_XPATH).count();
    }

    public void scrollGridTo(int xPosition) {
        Locator vp = page().locator(AG_CENTER_VIEWPORT);
        if (vp.count() > 0) {
            vp.first().evaluate("el => el.scrollLeft = " + xPosition);
            page().waitForTimeout(150);
        }
    }

    private void scrollGridStepRight() {
        page().locator(AG_CENTER_VIEWPORT).evaluate("el => el.scrollLeft += 300");
        page().waitForTimeout(150);
    }

    private int getHorizontalScrollSteps() {
        try {
            Locator vp = page().locator(AG_CENTER_VIEWPORT);
            if (vp.count() == 0)
                return 20;
            int scrollWidth = ((Number) vp.first().evaluate("el => el.scrollWidth")).intValue();
            int clientWidth = ((Number) vp.first().evaluate("el => el.clientWidth")).intValue();
            int steps = (Math.max(0, scrollWidth - clientWidth) / 300) + 2;
            return Math.min(steps, 40);
        } catch (Exception e) {
            return 20;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Filter Utilities ──────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    private static final String FILTER_BTN_XPATH = "//button[contains(@class, 'quick-filter')] | //div[img[@alt='filterAction']]";
    private static final String APPLIED_FILTER_XPATH = "//div[contains(@class, 'label-accordion-block quick-filter')] | //div[p[text()='Applied Filters']]";
    private static final String SET_FILTERS_XPATH = "//button[contains(@class, 'advance-filter-icon')] | //div[img[@alt='filterAction']]";
    private static final String APPLY_FILTER_XPATH = "(//div/a[contains(text(),'Apply Filters')])[last()] | (//div[contains(text(),'Apply Filters')])[last()]";
    private static final String CLEAR_ALL_XPATH = "//div[text()='Clear All']";

    public void openFilterPanel() {
        if (isVisible(APPLIED_FILTER_XPATH)) {
            page().locator(SET_FILTERS_XPATH).click();
        } else {
            waitForElement(FILTER_BTN_XPATH);
            page().locator(FILTER_BTN_XPATH).click();
        }
    }

    public void applyFilter() {
        LogUtils.info("Clicking Apply Filters");
        Locator btn = page().locator(APPLY_FILTER_XPATH);
        btn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));
        btn.click();
    }

    public void clearAllFilters() {
        page().locator(SET_FILTERS_XPATH).click();
        waitForPageLoad();
        page().waitForSelector(CLEAR_ALL_XPATH,
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(5_000));
        page().locator(CLEAR_ALL_XPATH).click();
    }

    /**
     * Applies an advanced filter by opening the panel, clearing existing filters,
     * filling in the provided field→value map, then clicking Apply.
     */
    public void advancedFilter(Map<String, String> fieldValueMap) {
        advancedFilter(fieldValueMap, true);
    }

    public void advancedFilter(Map<String, String> fieldValueMap, boolean isAnd) {
        waitForPageLoad();
        openFilterPanel();
        waitForPageLoad();
        clearAllFilters();
        setCardCondition(isAnd);
        fillFilterFields(fieldValueMap);
        applyFilter();
        waitForLoadingSpinner();
    }

    private void setCardCondition(boolean isAnd) {
        if (!isAnd) {
            try {
                Locator dd = page().locator(SET_CARD_CONDITION_XPATH);
                dd.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5_000));
                dd.click();
                page().locator(CARD_CONDITION_OPTION_XPATH).click();
            } catch (Exception ignored) {
            }
        }
    }

    private void fillFilterFields(Map<String, String> fieldValueMap) {
        for (Map.Entry<String, String> entry : fieldValueMap.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            try {
                waitForElement(ADD_FIELD_BTN_XPATH, 3);
                page().locator(ADD_FIELD_BTN_XPATH).click();
                staticWait(1);
                waitForElement(SELECT_FIELD_BTN_XPATH, 10);
                page().locator(SELECT_FIELD_BTN_XPATH).click();
                staticWait(1);
                waitForElement(LABEL_SEARCH_XPATH, 10);
                page().locator(LABEL_SEARCH_XPATH).fill(label);
                staticWait(1);
                String labelOption = String.format(LABEL_OPTION_XPATH_TEMPLATE, label);
                waitForElement(labelOption, 3);
                page().locator(labelOption).click();
                staticWait(1);
                // Fill text value
                String valueInput1 = String.format(VALUE_INPUT1_XPATH_TEMPLATE, label);
                String valueInput2 = String.format(VALUE_INPUT2_XPATH_TEMPLATE, label);
                if (waitForElement(valueInput1, 3)) {
                    page().locator(valueInput1).fill(value);
                } else if (waitForElement(valueInput2, 3)) {
                    page().locator(valueInput2).fill(value);
                }
                staticWait(1);
            } catch (Exception e) {
                LogUtils.warn("Filter field setup failed for '" + label + "': " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Quick Filter (single-field) ────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public void quickFilter(String fieldLabel, String fieldValue) {
        waitForPageLoad();
        String fieldXpath = String.format(QUICK_FILTER_FIELD_XPATH_TEMPLATE, fieldLabel);

        waitForElement(QUICK_FILTER_PROFILE_XPATH);
        page().locator(QUICK_FILTER_PROFILE_XPATH).click(new Locator.ClickOptions().setForce(true));
        waitForElement(QUICK_FILTER_SEARCH_OPTION_XPATH);
        page().locator(QUICK_FILTER_SEARCH_OPTION_XPATH).fill(fieldLabel);
        waitForElement(fieldXpath, 5);
        page().locator(fieldXpath).click();
        waitForElement(QUICK_FILTER_VALUE_INPUT_XPATH);
        page().locator(QUICK_FILTER_VALUE_INPUT_XPATH).fill(fieldValue);
        page().locator(QUICK_FILTER_VALUE_INPUT_XPATH).press("Enter");
        waitForPageLoad();
        waitForLoadingSpinner();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Table / Pagination ────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    private static final String PAGE_SIZE_BTN = "//div[contains(@class,'page-size-container')]//button";
    private static final String PAGINATION_OPTIONS = "//ul[contains(@class,'dropdown-menu')]//li";
    private static final String CURRENT_PAGE_SIZE = "//div[contains(@class,'custom-ag-pagination')]//button[contains(@class,'custom-ag-pagination__page-size-select')]/span";

    public String getCurrentPageSize() {
        waitForElement(CURRENT_PAGE_SIZE);
        return page().locator(CURRENT_PAGE_SIZE).innerText().trim();
    }

    public void setPageSize(String size) {
        waitForElement(PAGE_SIZE_BTN);
        page().locator(PAGE_SIZE_BTN).click();
        page().waitForTimeout(1000);
        String optionXpath = String.format(PAGE_SIZE_OPTION_XPATH_TEMPLATE, size);
        waitForElement(optionXpath);
        page().locator(optionXpath).click();
        page().waitForTimeout(2000);
    }

    public List<String> getAvailablePageSizes() {
        waitForElement(PAGE_SIZE_BTN);
        page().locator(PAGE_SIZE_BTN).click();
        page().waitForTimeout(1000);
        int count = page().locator(PAGINATION_OPTIONS).count();
        List<String> sizes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String txt = page().locator(PAGINATION_OPTIONS).nth(i).textContent().trim();
            if (!txt.isEmpty())
                sizes.add(txt);
        }
        return sizes;
    }

    public int getDisplayedRowCount() {
        String rowXpath = "//div[@class='dataTables_scrollBody']//table//tbody//tr"
                + " | //div[contains(@class,'ag-center-cols-container')]//div[contains(@class,'ag-row')]";
        waitForElement(rowXpath);
        return page().locator(rowXpath).count();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Row Selection ─────────────────────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public int selectRows(int count) {
        waitForPageLoad();
        staticWait(1);
        int selected = 0;
        for (int i = 1; i <= count; i++) {
            String cb = String.format(SELECT_ROW_CHECKBOX_XPATH_TEMPLATE, i);
            Locator loc = page().locator(cb);
            if (loc.count() == 0)
                break;
            loc.click(new Locator.ClickOptions().setForce(true));
            staticWait(1);
            selected++;
        }
        return selected;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── Misc Assertions / Utilities ───────────────────────────────────────
    // ═══════════════════════════════════════════════════════════════════════

    public void verifyModuleHeader(String headerName) {
        String xpath = String.format(MODULE_HEADER_XPATH_TEMPLATE, headerName);
        waitForElement(xpath);
        Assert.assertTrue(isVisible(xpath), "Module header not visible: " + headerName);
        captureScreenshot("module_header_" + headerName.replace(" ", "_"));
    }

    public void verifyColumnPresent(String... columnNames) {
        SoftAssert sa = new SoftAssert();
        for (String col : columnNames) {
            String xpath = String.format(AG_COLUMN_HEADER_XPATH_TEMPLATE, col);
            boolean visible = isVisible(xpath);
            if (visible)
                LogUtils.pass("Column visible: " + col);
            else {
                LogUtils.fail("Column missing: " + col);
                sa.fail("Column missing: " + col);
            }
        }
        sa.assertAll();
    }

    public void clickOkButton() {
        waitForElement(OK_BUTTON_XPATH);
        page().locator(OK_BUTTON_XPATH).click();
    }

    public void clickYesButton() {
        waitForElement(YES_BUTTON_XPATH);
        page().locator(YES_BUTTON_XPATH).click();
        staticWait(1);
    }

    public void clickSaveButton() {
        waitForElement(SAVE_BUTTON_XPATH);
        page().locator(SAVE_BUTTON_XPATH).click();
        waitForPageLoad();
    }

    public void clickCancelButton() {
        waitForElement(CANCEL_BUTTON_XPATH);
        page().locator(CANCEL_BUTTON_XPATH).click();
    }

    public void pause() {
        LogUtils.info("Pausing page execution (Playwright Inspector)...");
        page().pause();
    }

    // ── Random data generators ───────────────────────────────────────────

    public static String randomAlpha(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public static String randomNumeric(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    // ========== Get Attribute ==============
    public String getAttribute(String locator, String attribute) {
        return page().locator(locator).getAttribute(attribute);
    }

    // ========== Locator Helpers & Overloads ==========

    public Locator getByRole(com.microsoft.playwright.options.AriaRole role, String name) {
        return page().getByRole(role, new Page.GetByRoleOptions().setName(name));
    }

    public Locator getByText(String text) {
        return page().getByText(text);
    }

    public Locator getById(String id) {
        return page().locator("id=" + id);
    }

    public void highlightElement(String selector) {
        try {
            page().evaluate("el => el.style.border = '3px solid red'",
                    page().locator(selector).first().elementHandle());
        } catch (Exception e) {
            LogUtils.warn("Could not highlight element: " + selector);
        }
    }

    public void highlightElement(Locator locator) {
        try {
            page().evaluate("el => el.style.border = '3px solid red'", locator.first().elementHandle());
        } catch (Exception e) {
            LogUtils.warn("Could not highlight element via locator");
        }
    }

    public void scrollIntoView(Locator locator) {
        locator.first().evaluate("el => el.scrollIntoView({ behavior: 'instant', block: 'center' })");
        page().waitForTimeout(300);
    }

    public String getText(Locator locator) {
        waitForElement(locator);
        return locator.first().textContent().trim();
    }

    public boolean isVisible(Locator locator) {
        try {
            return locator.first().isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementPresent(Locator locator) {
        try {
            return locator.count() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
