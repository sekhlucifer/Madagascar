package com.framework.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.Sources;

/**
 * Typed configuration interface loaded from multi-layer property files.
 *
 * <p>
 * Priority (highest → lowest):
 * <ol>
 * <li>System properties (-Dkey=value)</li>
 * <li>Environment-specific configs/{env}/config.properties</li>
 * <li>Common defaults configs/common_config.properties</li>
 * </ol>
 */
@LoadPolicy(Config.LoadType.MERGE)
@Sources({
        "system:properties",
        "file:configs/${env}/config.properties",
        "file:configs/common_config.properties"
})
public interface Configuration extends Config {

    // ── Application URLs ─────────────────────────────────────────────────────

    @Key("base.url")
    String baseUrl();

    @Key("orderlist.path")
    String orderlistPath();

    @Key("login.path")
    @DefaultValue("/login")
    String loginPath();

    @Key("home.path")
    @DefaultValue("/home")
    String homePath();

    // ── API ──────────────────────────────────────────────────────────────────

    @Key("api.base.url")
    String apiBaseUrl();

    @Key("api.key")
    @DefaultValue("")
    String apiKey();

    // ── Credentials ──────────────────────────────────────────────────────────

    @Key("app.username")
    String appUsername();

    @Key("app.password")
    String appPassword();

    // ── Browser / Playwright ─────────────────────────────────────────────────

    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("browser.persistent")
    @DefaultValue("false")
    Boolean browserPersistent();

    @Key("browser.user.data.dir")
    @DefaultValue("")
    String browserUserDataDir();

    @Key("browser.profile.name")
    @DefaultValue("")
    String browserProfileName();

    @Key("browser.storage.state.enabled")
    @DefaultValue("false")
    Boolean browserStorageStateEnabled();

    @Key("browser.storage.state.path")
    @DefaultValue("configs/user.json")
    String browserStorageStatePath();

    @Key("headless")
    @DefaultValue("false")
    Boolean headless();

    @Key("slow.motion")
    @DefaultValue("0")
    int slowMotion();

    @Key("timeout")
    @DefaultValue("30000")
    int timeout();

    // ── Reporting / Artefacts ────────────────────────────────────────────────

    @Key("base.screenshot.path")
    @DefaultValue("reports/screenshots")
    String screenshotPath();

    @Key("base.report.path")
    @DefaultValue("reports/extent")
    String reportPath();

    @Key("base.traces.path")
    @DefaultValue("reports/traces")
    String tracesPath();

    @Key("base.test.data.path")
    @DefaultValue("src/test/resources/testdata")
    String testDataPath();

    // ── Database ─────────────────────────────────────────────────────────────

    @Key("db.url")
    @DefaultValue("")
    String dbUrl();

    @Key("db.username")
    @DefaultValue("")
    String dbUsername();

    @Key("db.password")
    @DefaultValue("")
    String dbPassword();

    // ── Retry ────────────────────────────────────────────────────────────────

    @Key("retry.count")
    @DefaultValue("1")
    int retryCount();

    // ── Jira / Xray Integration ───────────────────────────────────────────────

    @Key("jira.excel.tracking.enabled")
    @DefaultValue("false")
    Boolean jiraExcelTrackingEnabled();

    @Key("jira.excel.file.path")
    @DefaultValue("reports/jira_test_results.xlsx")
    String jiraExcelFilePath();
}
