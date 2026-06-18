# Playwright Java Automation Framework

Enterprise-grade, parallel-safe UI + API test automation framework built on **Java 17**, **Playwright 1.50**, and **TestNG 7.9**.

---
<!-- .\gradlew.bat test --tests "com.framework.tests.TOI_UM_WorkFlow.ToI_UM_WorkFlow" -->
## Architecture

```
playwright-java-framework/
├── src/main/java/com/framework/
│   ├── annotations/        # @JIRA, TestGroups constants
│   ├── browser/            # BrowserFactory (enum), BrowserManager (ThreadLocal lifecycle)
│   ├── config/             # Configuration (Owner interface), ConfigurationManager
│   ├── context/            # TestContext (thread-local in-test data store)
│   ├── enums/              # BrowserType, Environment, WaitStrategy
│   ├── exceptions/         # AutomationException, ElementNotFoundException
│   ├── listeners/          # TestListener, RetryAnalyzer, RetryTransformer
│   ├── pages/              # BasePage (all reusable Playwright interactions)
│   ├── reporting/          # ExtentManager (singleton), ExtentReport (ThreadLocal)
│   ├── tests/              # BaseTest (BeforeMethod/AfterMethod hooks)
│   └── utils/              # LogUtils, RestClient, DBUtil, ExcelUtil, JsonUtil,
│                           #  DateUtil, RandomDataUtil, FileUtil, Assertion, WaitUtil
├── src/test/java/com/framework/tests/
│   └── sample/             # LoginPage, LoginTests  ← starter examples
├── src/test/resources/testng/
│   ├── testng.xml          # Full suite (smoke + regression + all)
│   └── testng-smoke.xml    # Smoke-only suite
├── configs/
│   ├── common_config.properties   # Shared defaults (browser, timeout, paths)
│   ├── dev/config.properties
│   ├── qa/config.properties
│   └── staging/config.properties
├── pipeline/
│   └── azure-pipelines.yml
└── .github/workflows/
    └── automation.yml
```

---

## Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17+     |
| Gradle | 8.5+ (wrapper included) |
| Chrome / Firefox / Edge | Latest |

### 1. Clone & install browsers

```bash
git clone <repo-url>
cd playwright-java-framework
./gradlew installPlaywright
```

### 2. Configure your environment

Copy and fill in the target environment config:

```bash
cp configs/qa/config.properties.template configs/qa/config.properties
# Edit base.url, app.username, app.password, api.base.url, db.url …
```

### 3. Run tests

```bash
# All tests — QA env, Chrome, headed
./gradlew test -Denv=qa -Dbrowser=chrome

# Smoke only — headless
./gradlew test -Denv=qa -Dbrowser=chrome -Dheadless=true -Dgroups=smoke

# Firefox, staging
./gradlew test -Denv=staging -Dbrowser=firefox -Dheadless=true

# Specific test class
./gradlew test -Denv=qa --tests "com.framework.tests.sample.LoginTests"
```

Reports are generated in `reports/extent/report_<timestamp>.html`.

---

## Adding a New Page Object

```java
// src/test/java/com/yourapp/pages/DashboardPage.java
public class DashboardPage extends BasePage {

    private static final String WELCOME_MSG = "//h1[@class='welcome-header']";

    public String getWelcomeMessage() {
        waitForElement(WELCOME_MSG);
        return getText(WELCOME_MSG);
    }
}
```

## Adding a New Test

```java
// src/test/java/com/yourapp/tests/DashboardTests.java
public class DashboardTests extends BaseTest {

    @Test(description = "Welcome message is displayed after login", groups = {TestGroups.SMOKE})
    @JIRA(id = "DASH-001")
    public void welcomeMessageIsDisplayed() {
        new LoginPage().open().loginWithConfigCredentials();
        String msg = new DashboardPage().getWelcomeMessage();
        Assertion.assertContains(msg, "Welcome", "Welcome message should contain 'Welcome'");
    }
}
```

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **ThreadLocal** for Page/Browser/Context | Safe parallel execution without `synchronized` |
| **Owner** for config | Type-safe, multi-source, cacheable config |
| **ExtentReports** per-thread | No report mixing in parallel runs |
| **RetryTransformer** global retry | No per-test annotation needed |
| **BasePage** abstract + inherited | Single source of truth for all Playwright helpers |
| **TestContext** thread-local data store | Pass data between steps without test fields |

---

## CI/CD

### GitHub Actions

Triggered on push, PR, nightly schedule, or manual dispatch.  
Artifacts: Extent report, failure screenshots, logs.

```
.github/workflows/automation.yml
```

### Azure DevOps

Two-stage pipeline: **Smoke Gate → Regression**.  
```
pipeline/azure-pipelines.yml
```

Required pipeline secret variables: `APP_PASSWORD`, `API_KEY`, `DB_PASSWORD`.

---

## Configuration Reference

| Key | Default | Description |
|-----|---------|-------------|
| `browser` | `chrome` | `chrome / chromium / firefox / edge / webkit` |
| `headless` | `false` | Run browsers without a visible window |
| `timeout` | `30000` | Default Playwright element timeout (ms) |
| `slow.motion` | `0` | Delay between each Playwright action (ms) |
| `retry.count` | `1` | How many times to retry a failing test |
| `base.url` | — | Application base URL (per env) |
| `api.base.url` | — | API base URL (per env) |
| `db.url` | — | JDBC URL (per env) |

Override any key at runtime:  `./gradlew test -Dtimeout=60000`

---

## Extending the Framework

### Custom API client

```java
public class UserApiClient extends RestClient {
    public UserApiClient() { super(TestContext.getString("authToken")); }
    public Response getUser(String id) { return get("/users/" + id); }
}
```

### Database setup / teardown

```java
List<Map<String,String>> rows = DBUtil.executeQuery(
    "SELECT * FROM bookings WHERE status = ?", "PENDING");
```

### Shared test data via TestContext

```java
TestContext.put("containerId", "ABCD1234567");
// In another step:
String id = TestContext.getString("containerId");
```
