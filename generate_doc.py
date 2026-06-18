import sys
import os
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, KeepTogether, PageBreak
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.lib.colors import HexColor
from reportlab.pdfgen import canvas

# =========================================================================
# NUMBERED CANVAS - DOUBLE PASS PAGINATION & RUNNING HEADERS/FOOTERS
# =========================================================================
class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_decorations(self, page_count):
        if self._pageNumber == 1:
            # Draw Cover Page structural border accents
            self.saveState()
            self.setStrokeColor(HexColor('#0F766E')) # Teal-700
            self.setLineWidth(4)
            self.line(54, 738, 54, 54) # Elegant left border
            self.setStrokeColor(HexColor('#0F172A')) # Navy-900
            self.setLineWidth(1)
            self.line(54, 54, 558, 54) # Bottom border
            self.restoreState()
            return
            
        self.saveState()
        
        # 1. Running Header
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(HexColor('#0F172A')) # Deep Navy
        self.drawString(54, 752, "PLAYWRIGHT JAVA AUTOMATION FRAMEWORK")
        
        self.setFont("Helvetica", 8)
        self.setFillColor(HexColor('#64748B')) # Slate-500
        self.drawRightString(558, 752, "TECHNICAL ARCHITECTURE & IMPLEMENTATION GUIDE")
        
        # Header Horizontal Rule
        self.setStrokeColor(HexColor('#CBD5E1')) # Slate-200
        self.setLineWidth(0.5)
        self.line(54, 744, 558, 744)
        
        # 2. Running Footer
        self.line(54, 52, 558, 52)
        self.drawString(54, 40, "CONFIDENTIAL - ENTERPRISE AUTOMATION DEPLOYMENT")
        self.drawRightString(558, 40, f"Page {self._pageNumber} of {page_count}")
        
        self.restoreState()

# =========================================================================
# HELPER FUNCTIONS FOR RENDERING RICH STYLES
# =========================================================================
def escape_html(text):
    return text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')

def format_code(text):
    # Prepare text for ReportLab Paragraph compatibility
    escaped = escape_html(text)
    # Replace tabs with non-breaking spaces
    escaped = escaped.replace('\t', '&nbsp;' * 4)
    # Replace single space with non-breaking space (carefully to preserve wraps)
    lines = escaped.split('\n')
    formatted_lines = []
    for line in lines:
        # replace leading spaces to keep indentation
        leading_spaces = len(line) - len(line.lstrip(' '))
        line = ('&nbsp;' * leading_spaces) + line.lstrip(' ')
        formatted_lines.append(line)
    return '<br/>'.join(formatted_lines)

def make_code_block(code_text, body_style):
    formatted = format_code(code_text)
    p = Paragraph(f"<font face='Courier' size='7.5' color='#1E293B'>{formatted}</font>", body_style)
    t = Table([[p]], colWidths=[504])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), HexColor('#F8FAFC')),
        ('BOX', (0,0), (-1,-1), 0.5, HexColor('#E2E8F0')),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    return t

def make_table(headers, data, col_widths=None, body_style=None):
    table_data = []
    
    # Headers
    formatted_headers = []
    for h in headers:
        p = Paragraph(f"<b><font color='white'>{h}</font></b>", ParagraphStyle('HdrStyle', parent=body_style, fontSize=9, leading=11))
        formatted_headers.append(p)
    table_data.append(formatted_headers)
    
    # Rows
    for row in data:
        formatted_row = []
        for cell in row:
            if isinstance(cell, str):
                p = Paragraph(cell, ParagraphStyle('CellStyle', parent=body_style, fontSize=8.5, leading=11))
                formatted_row.append(p)
            else:
                formatted_row.append(cell)
        table_data.append(formatted_row)
        
    t = Table(table_data, colWidths=col_widths)
    t_style = [
        ('BACKGROUND', (0,0), (-1,0), HexColor('#0F172A')), # Dark navy
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
        ('GRID', (0,0), (-1,-1), 0.5, HexColor('#CBD5E1')),
    ]
    # Alternating row background
    for i in range(1, len(data) + 1):
        if i % 2 == 0:
            t_style.append(('BACKGROUND', (0,i), (-1,i), HexColor('#F8FAFC')))
    t.setStyle(TableStyle(t_style))
    return t

def make_callout(text, type='info', body_style=None):
    bg_color = HexColor('#F0FDFA') if type == 'info' else HexColor('#FFFBEB') if type == 'warning' else HexColor('#F8FAFC')
    border_color = HexColor('#0F766E') if type == 'info' else HexColor('#D97706') if type == 'warning' else HexColor('#0F172A')
    
    label = "INFO" if type == 'info' else "WARNING" if type == 'warning' else "DESIGN NOTE"
    p = Paragraph(f"<b><font color='{border_color.hexval()}'>{label}:</font></b> {text}", ParagraphStyle('CallStyle', parent=body_style, fontSize=8.5, leading=12))
    
    t = Table([[p]], colWidths=[504])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), bg_color),
        ('LINELEFT', (0,0), (0,-1), 3, border_color),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 12),
        ('RIGHTPADDING', (0,0), (-1,-1), 12),
        ('BOX', (0,0), (-1,-1), 0.5, HexColor('#E2E8F0')),
    ]))
    return t

# =========================================================================
# MAIN GENERATION PROCESS
# =========================================================================
def generate_pdf():
    pdf_filename = "Playwright_Java_Framework_Documentation.pdf"
    
    # 1. Setup Document Template
    # Page size: Letter (612 x 792 pt). Margins: Left/Right 54 pt (0.75in), Top/Bottom 72 pt (1.0in)
    # Printable Width: 504 pt
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=72,
        bottomMargin=72
    )
    
    story = []
    
    # 2. Setup Styles
    styles = getSampleStyleSheet()
    
    body_style = ParagraphStyle(
        'MainBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=HexColor('#334155'), # Slate-700
        spaceAfter=8
    )
    
    bullet_style = ParagraphStyle(
        'MainBullet',
        parent=body_style,
        leftIndent=15,
        firstLineIndent=-10,
        spaceAfter=4
    )
    
    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=30,
        textColor=HexColor('#0F172A'), # Navy-900
        alignment=0,
        spaceAfter=12
    )
    
    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=12,
        leading=16,
        textColor=HexColor('#0F766E'), # Teal-700
        alignment=0,
        spaceAfter=30
    )
    
    h1_style = ParagraphStyle(
        'Heading1',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=16,
        leading=20,
        textColor=HexColor('#0F172A'),
        spaceBefore=16,
        spaceAfter=10,
        keepWithNext=True
    )
    
    h2_style = ParagraphStyle(
        'Heading2',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11.5,
        leading=15,
        textColor=HexColor('#0F766E'),
        spaceBefore=12,
        spaceAfter=6,
        keepWithNext=True
    )

    toc_section_style = ParagraphStyle(
        'TOCSec',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=12,
        textColor=HexColor('#0F172A')
    )

    toc_page_style = ParagraphStyle(
        'TOCPage',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=12,
        textColor=HexColor('#0F766E'),
        alignment=2 # Right align
    )
    
    # =========================================================================
    # PAGE 1: COVER PAGE
    # =========================================================================
    story.append(Spacer(1, 40))
    story.append(Paragraph("<b>ENTERPRISE PLAYWRIGHT JAVA</b>", ParagraphStyle('CoverPre', fontName='Helvetica-Bold', fontSize=12, leading=14, textColor=HexColor('#64748B'), spaceAfter=4)))
    story.append(Paragraph("TEST AUTOMATION FRAMEWORK", title_style))
    story.append(Paragraph("Technical Architecture, Core Operations Manual &amp; Implementation Guide", subtitle_style))
    
    story.append(Spacer(1, 20))
    
    # Cover Metadata Grid
    metadata_data = [
        ["Core Platform Stack", "Java 17/21 LTS, Playwright 1.50.0, TestNG 7.9.0"],
        ["Reporting Engine", "ExtentReports 5.0.9 (Parallel HTML reporting)"],
        ["Configuration Engine", "Aeonbits Owner 1.0.12 (Type-safe, property-backed)"],
        ["Scope", "End-to-End UI, Parallel Thread-Isolated Web Execution, API &amp; Database Testing"],
        ["Target Environments", "Dev, QA, Staging, Production"],
        ["Author / Team", "Enterprise QA Core Engineering Team"],
        ["Release Version", "v1.0.0 (Production Stable)"],
        ["Documentation Date", "May 26, 2026"]
    ]
    
    meta_table = make_table(
        ["Framework Attribute", "Enterprise Specifications &amp; Mappings"],
        metadata_data,
        col_widths=[150, 354],
        body_style=body_style
    )
    story.append(meta_table)
    
    story.append(Spacer(1, 140))
    story.append(Paragraph("<font size='8' color='#64748B'><b>NOTICE:</b> This document contains proprietary architectural designs, coding standards, and deployment pipelines. Access is restricted to engineering and QA teams authorized to modify or run automated suites.</font>", body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 2: TABLE OF CONTENTS
    # =========================================================================
    story.append(Spacer(1, 10))
    story.append(Paragraph("TABLE OF CONTENTS", h1_style))
    story.append(Paragraph("Navigate the components and guides of the test automation engine:", body_style))
    story.append(Spacer(1, 10))
    
    toc_entries = [
        ["1. Executive Summary &amp; Core Highlights", "Page 3"],
        ["   1.1 Technology Stack &amp; Direct Dependencies", "Page 3"],
        ["2. System Architecture &amp; Repository Layout", "Page 3"],
        ["   2.1 Key Folders &amp; Lifecycle Responsibility Map", "Page 4"],
        ["3. Architectural Design Decisions &amp; Thread Safety", "Page 4"],
        ["   3.1 ThreadLocal Lifecycles (Browser, Context, Page)", "Page 4"],
        ["   3.2 Type-Safe Config Management (Owner &amp; CLI Overrides)", "Page 5"],
        ["   3.3 Thread-Safe Dashboarding &amp; Global Listeners", "Page 5"],
        ["4. Setup, Environment Initialization &amp; CLI Orchestration", "Page 5"],
        ["   4.1 Local Installation", "Page 5"],
        ["   4.2 Environment Configuration Structure", "Page 6"],
        ["   4.3 Command Line Interface execution commands", "Page 6"],
        ["5. Step-by-Step Code Recipes &amp; Extensions", "Page 7"],
        ["   5.1 Creating a Robust Page Object Model", "Page 7"],
        ["   5.2 Writing Parallel-Ready Test Classes", "Page 8"],
        ["   5.3 Dynamic Database Assertions", "Page 8"],
        ["   5.4 Lightweight API Verification", "Page 8"],
        ["6. Continuous Integration &amp; Deployment Pipelines", "Page 9"],
        ["   6.1 GitHub Actions Workflow", "Page 9"],
        ["   6.2 Azure DevOps Multi-Stage Execution", "Page 9"],
        ["7. Design Guidelines &amp; Quality Best Practices", "Page 10"],
        ["   7.1 Locator Best Practices", "Page 10"],
        ["   7.2 Page-State &amp; Context Best Practices", "Page 10"]
    ]
    
    toc_table_rows = []
    for label, pg in toc_entries:
        bold = "<b>" in label or label.strip().startswith(("1.", "2.", "3.", "4.", "5.", "6.", "7."))
        if bold:
            left = Paragraph(f"<b>{label}</b>", toc_section_style)
            right = Paragraph(f"<b>{pg}</b>", toc_page_style)
        else:
            left = Paragraph(f"<font color='#475569'>{label}</font>", ParagraphStyle('TOCSub', parent=body_style, fontSize=9, leading=12))
            right = Paragraph(f"<font color='#0F766E'>{pg}</font>", ParagraphStyle('TOCSubP', parent=body_style, fontSize=9, leading=12, alignment=2))
        toc_table_rows.append([left, right])
        
    toc_table = Table(toc_table_rows, colWidths=[400, 104])
    toc_table.setStyle(TableStyle([
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('LINEBELOW', (0,0), (-1,-1), 0.25, HexColor('#F1F5F9')),
    ]))
    story.append(toc_table)
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 3: EXECUTIVE SUMMARY & TECHNOLOGY STACK
    # =========================================================================
    story.append(Paragraph("1. Executive Summary &amp; Core Highlights", h1_style))
    story.append(Paragraph("This Playwright Java Test Automation Framework represents an enterprise-grade, highly scalable testing platform engineered for high-concurrency UI and API validation. It is specifically designed to eliminate common test automation pain points, such as browser session contamination, resource leakage, unmaintainable configuration mappings, and rigid selector dependencies.", body_style))
    
    story.append(Paragraph("By combining Java's type safety and object-oriented strength with Microsoft Playwright's blazing fast, multi-threaded browser control and TestNG's rich suite orchestrations, the framework delivers extremely robust execution. Tests execute concurrently without race conditions, and complete HTML report dashboards with screenshot evidence are generated automatically on every execution.", body_style))
    
    story.append(Paragraph("1.1 Technology Stack &amp; Direct Dependencies", h2_style))
    story.append(Paragraph("The system is managed under Gradle, leveraging locked, enterprise-stable dependency coordinates to guarantee deterministic builds in both local and CI/CD environments. Below is a breakdown of our standardized tech stack:", body_style))
    
    tech_stack_data = [
        ["Playwright Java", "1.50.0", "Fast cross-browser execution, native Shadow DOM extraction, network interception, auto-wait locators."],
        ["TestNG", "7.9.0", "Parallel thread pool execution, customized listener hooks, parameter mapping, robust test suite grouping."],
        ["ExtentReports", "5.0.9", "Rich dashboarding with embedded screenshot evidence, multi-thread isolated reporting, detailed timelines."],
        ["Owner Library", "1.0.12", "Type-safe interface mapping to property configurations, automatic type coercion, and dynamic runtime property loading."],
        ["Lombok", "1.18.30", "Automated code generation to eliminate boilerplate logging, getters, setters, and constructors in data objects."],
        ["RestAssured", "5.4.0", "Advanced REST API verification, clean DSL syntax, automatic JSON/XML payload parsing and serialization."],
        ["Apache POI", "5.2.5", "High-performance Excel parsing and utility structures to handle data-driven test arrays and external files."]
    ]
    
    tech_table = make_table(
        ["Framework Library", "Locked Version", "Core Architectural Responsibility &amp; Benefit"],
        tech_stack_data,
        col_widths=[110, 80, 314],
        body_style=body_style
    )
    story.append(tech_table)
    
    story.append(Spacer(1, 15))
    story.append(Paragraph("2. System Architecture &amp; Repository Layout", h1_style))
    story.append(Paragraph("The repository is strictly structured into modular components, isolating technical configuration layers from standard test pages and executable test classes. This architectural pattern prevents cross-layer pollution and ensures high code maintainability.", body_style))
    
    dir_structure = """playwright-java-framework/
├── src/main/java/com/framework/
│   ├── annotations/        # @JIRA metadata, custom test groups constants
│   ├── browser/            # BrowserFactory (enum), BrowserManager (ThreadLocal lifecycles)
│   ├── config/             # Configuration interface (Owner), ConfigurationManager singleton
│   ├── context/            # TestContext (thread-local in-test data sharing store)
│   ├── enums/              # BrowserType, Environment, WaitStrategy definitions
│   ├── exceptions/         # Custom AutomationException / ElementNotFoundException definitions
│   ├── listeners/          # TestListener, RetryAnalyzer, RetryTransformer (dynamic suite retries)
│   ├── pages/              # BasePage (reusable Playwright wrappers), page objects
│   ├── reporting/          # ExtentManager (singleton), ExtentReport (ThreadLocal test instance)
│   ├── tests/              # BaseTest (BeforeMethod/AfterMethod lifecycle hooks)
│   └── utils/              # LogUtils, RestClient, DBUtil, ExcelUtil, DateUtil, Assertion
├── src/test/java/com/framework/tests/
│   └── sample/             # Page classes & Test suites (AdminPage, AdminTests, etc.)
└── configs/                # Environment configuration folder (common, dev, qa, staging)"""
    
    story.append(make_code_block(dir_structure, body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 4: DETAILED LIFECYCLE RESPONSIBILITY & KEY DESIGN DECISIONS
    # =========================================================================
    story.append(Paragraph("2.1 Lifecycle Responsibility Map", h2_style))
    story.append(Paragraph("To keep the codebase maintainable, each subdirectory in `com/framework/` has a highly specific responsibility. This enforces separation of concerns across the automated test execution stack:", body_style))
    
    folders_data = [
        ["annotations", "Contains custom Java annotations such as @JIRA to link test executions to Jira tickets and define custom group constants."],
        ["browser", "The core browser initialization logic. Hosts the BrowserFactory to spawn browsers and BrowserManager to isolate page state."],
        ["config", "Maintains type-safe environment configurations, loaded from common and environment-specific .properties files."],
        ["context", "Implements TestContext, providing a ThreadLocal map for tests to share data (e.g., dynamic login tokens) across different steps."],
        ["listeners", "Contains custom TestNG listeners that intercept test failures, capture automatic screenshots, and handle flaky test retries."],
        ["pages", "Contains the core BasePage wrapper class, which hides direct Playwright calls behind fluent, auto-waiting wrappers."],
        ["reporting", "Coordinates thread-safe ExtentReports generation so concurrent threads write results without cross-report corruption."],
        ["tests", "Hosts BaseTest, the parent class for all test classes. Sets up browser threads and handles automatic teardowns."]
    ]
    
    folders_table = make_table(
        ["Component Package", "Lifecycle Responsibility &amp; Execution Roles"],
        folders_data,
        col_widths=[120, 384],
        body_style=body_style
    )
    story.append(folders_table)
    
    story.append(Spacer(1, 10))
    story.append(Paragraph("3. Architectural Design Decisions &amp; Thread Safety", h1_style))
    story.append(Paragraph("Engineering parallel UI automation in Java is notoriously difficult due to multi-threaded interactions with browser sessions. The framework implements several advanced mechanisms to guarantee parallel safety and speed:", body_style))
    
    story.append(Paragraph("3.1 ThreadLocal Lifecycles (Browser, Context, Page)", h2_style))
    story.append(Paragraph("To run tests in parallel without session overlap, the browser lifecycle is strictly bound to the execution thread using Java's <code>ThreadLocal</code>. Inside <code>BrowserManager.java</code>, three main components are isolated per-thread:", body_style))
    
    threadlocal_code = """// Excerpt from BrowserManager.java demonstrating thread isolation
private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

public static Page getPage() {
    return pageThreadLocal.get();
}

public static void initPage() {
    Page page = contextThreadLocal.get().newPage();
    pageThreadLocal.set(page);
}"""
    story.append(make_code_block(threadlocal_code, body_style))
    story.append(Spacer(1, 5))
    story.append(make_callout("This ThreadLocal isolation ensures that each parallel thread operates on a fully isolated Page and BrowserContext. Standard variables are never shared, eliminating race conditions entirely.", "info", body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 5: CONFIG ENGINE, DYNAMIC RETRIES & QUICK START SETUP
    # =========================================================================
    story.append(Paragraph("3.2 Type-Safe Config Management (Owner &amp; CLI Overrides)", h2_style))
    story.append(Paragraph("Environment configurations are loaded via the <b>Aeonbits Owner</b> library. Instead of writing manual file-readers and parsing strings, we define a standard Java Interface. The properties are mapped to types automatically, and support default values and fallback paths:", body_style))
    
    config_code = """@Config.Sources({
    "system:properties",
    "file:configs/${env}/config.properties",
    "file:configs/common_config.properties"
})
public interface FrameworkConfig extends Config {
    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("base.url")
    String baseUrl();

    @Key("timeout")
    @DefaultValue("30000")
    int timeout();
}"""
    story.append(make_code_block(config_code, body_style))
    story.append(Spacer(1, 5))
    story.append(Paragraph("This configuration setup allows for seamless runtime command-line overrides. For example, executing <code>./gradlew test -Dbrowser=firefox -Denv=staging</code> dynamically resolves the values through the <code>${env}</code> interpolation and system properties injection.", body_style))
    
    story.append(Paragraph("3.3 Thread-Safe Dashboarding &amp; Global Listeners", h2_style))
    story.append(Paragraph("The logging, retry mechanisms, and ExtentReports generation utilize global TestNG listeners to keep test classes completely clean:", body_style))
    
    story.append(Paragraph("<b>• ExtentReports Per-Thread Isolation:</b> The `ExtentReport` wrapper uses `ThreadLocal<ExtentTest>` to isolate reporting nodes. During parallel runs, thread A writes logs into its own ExtentTest instance, and thread B writes to its instance. When tests complete, the instances are seamlessly merged into a single index dashboard.", bullet_style))
    
    story.append(Paragraph("<b>• RetryTransformer Interceptor:</b> Flaky tests in cloud pipelines can cause false-alarm alerts. The `RetryTransformer` listener automatically implements the TestNG `IAnnotationTransformer` interface. At suite initialization, it dynamically binds our `RetryAnalyzer` to all tests, ensuring failed tests are retried <code>N</code> times automatically (configured via <code>retry.count</code> properties) without needing manual method annotations.", bullet_style))
    
    story.append(Spacer(1, 10))
    story.append(Paragraph("4. Setup, Environment Initialization &amp; CLI Orchestration", h1_style))
    story.append(Paragraph("Setting up the framework is simple and designed for seamless onboardings of new developers and QA automation engineers.", body_style))
    
    story.append(Paragraph("4.1 Local Installation", h2_style))
    story.append(Paragraph("Ensure JDK 17 (or Java 21) is installed. Clone the repository and install the Playwright browsers using the preconfigured Gradle wrapper task:", body_style))
    
    setup_commands = """# 1. Clone repository
git clone <repository-url>
cd playwright-java-framework

# 2. Run Playwright installation to download isolated chromium, firefox, webkit binaries
./gradlew installPlaywright"""
    story.append(make_code_block(setup_commands, body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 6: ENV CONFIGURATION & CLI EXECUTION REFERENCE
    # =========================================================================
    story.append(Paragraph("4.2 Environment Configuration Structure", h2_style))
    story.append(Paragraph("Environment configurations are kept inside the `configs/` root directory. Variables that are shared (e.g., base timeouts, reporting paths, slow-motion intervals) are declared in `common_config.properties`, while environment-specific routes are isolated in dev/qa/staging subfolders:", body_style))
    
    env_config_example = """# configs/common_config.properties
timeout=30000
slow.motion=0
retry.count=1
screenshot.path=reports/extent/screenshots/

# configs/qa/config.properties
base.url=https://opensource-demo.orangehrmlive.com/
home.path=web/index.php/dashboard/index
api.base.url=https://api.qa.opensource-demo.org/v1/
db.url=jdbc:mysql://qa-rds.mysql.database.azure.com:3306/hr_db"""
    story.append(make_code_block(env_config_example, body_style))
    
    story.append(Paragraph("4.3 Command Line Interface Execution Commands", h2_style))
    story.append(Paragraph("Test suites are completely parameterizable from terminal commands. By overriding the <code>env</code>, <code>browser</code>, and <code>headless</code> system properties, engineers and CI/CD pipelines can control the run dynamics on-the-fly:", body_style))
    
    cli_runs = [
        ["Run full regression suite on QA using headless Chrome", "./gradlew test -Denv=qa -Dbrowser=chrome -Dheadless=true"],
        ["Run smoke suite on Staging using headed Firefox", "./gradlew test -Denv=staging -Dbrowser=firefox -Dheadless=false -Dgroups=smoke"],
        ["Execute a highly specific test class file", "./gradlew test -Denv=qa --tests \"com.framework.tests.sample.LoginTests\""],
        ["Override standard Playwright element timeout to 60s", "./gradlew test -Denv=qa -Dtimeout=60000"]
    ]
    
    cli_table = make_table(
        ["Target Automation Run Scenario", "Gradle Execution Command Line String"],
        cli_runs,
        col_widths=[214, 290],
        body_style=body_style
    )
    story.append(cli_table)
    
    story.append(Spacer(1, 10))
    story.append(Paragraph("4.4 Configuration Reference Properties", h2_style))
    story.append(Paragraph("Here is a detailed map of all core configuration keys, default values, and operational impacts:", body_style))
    
    config_keys_data = [
        ["browser", "chrome", "Directs Playwright to launch chrome, chromium, firefox, edge, or webkit."],
        ["headless", "false", "Determines if the browser launches with a visible UI window (false) or background mode (true)."],
        ["timeout", "30000", "Defines the default maximum wait-time in milliseconds for all page actions and locators."],
        ["slow.motion", "0", "Forces an artificial delay in milliseconds between each Playwright interaction (useful for debugging)."],
        ["retry.count", "1", "Sets the maximum number of attempts to rerun a failing test before flagging it as failed in reports."],
        ["base.url", "—", "The base web URL mapping of the target environment, referenced dynamically via BasePage."]
    ]
    
    config_keys_table = make_table(
        ["Configuration Key Name", "Default Value", "Technical Description &amp; Usage Mapping"],
        config_keys_data,
        col_widths=[104, 80, 320],
        body_style=body_style
    )
    story.append(config_keys_table)
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 7: STEP-BY-STEP RECIPES: CREATING A PAGE OBJECT MODEL
    # =========================================================================
    story.append(Paragraph("5. Step-by-Step Code Recipes &amp; Extensions", h1_style))
    story.append(Paragraph("A primary goal of the framework is to enable rapid script creation with minimal boilerplate. The following recipes demonstrate how to write page objects, automate tests, integrate backend validation, and execute lightweight API steps.", body_style))
    
    story.append(Paragraph("5.1 Creating a Robust Page Object Model", h2_style))
    story.append(Paragraph("Page classes MUST inherit from <code>BasePage</code>. BasePage exposes direct thread-isolated browser capabilities and utility wraps (like wait, click, fill, scroll, dropdown select) automatically. Avoid hardcoding Playwright locators inside tests; encapsulate them completely within Page Classes:", body_style))
    
    page_code = """package com.framework.pages;

import com.microsoft.playwright.Locator;

public class AdminPage extends BasePage {

    // 1. Encapsulate XPaths / Selectors in private final strings
    private static final String ADMIN_MENU_ITEM = "//a[contains(.,'Admin')]";
    private static final String SYSTEM_USERS_HEADER = "//h5[text()='System Users']";
    private static final String SEARCH_USERNAME_FIELD = "//div[label[text()='Username']]/following-sibling::div/input";
    private static final String SEARCH_BTN = "button[type='submit']";

    // 2. Build fluent, logical action methods
    public AdminPage navigateToAdminMenu() {
        click(ADMIN_MENU_ITEM);
        waitForElement(SYSTEM_USERS_HEADER);
        return this;
    }

    public AdminPage searchForUser(String username) {
        fill(SEARCH_USERNAME_FIELD, username);
        click(SEARCH_BTN);
        waitForLoadingSpinner();
        return this;
    }

    public String getCurrentPageUrl() {
        return page().url();
    }
}"""
    story.append(make_code_block(page_code, body_style))
    story.append(Spacer(1, 5))
    story.append(make_callout("Notice how each action method returns the Page instance (<code>return this</code>). This facilitates method chaining inside the test suites, creating clean, descriptive fluent flows.", "tip", body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 8: WRITING TEST CLASSES, DB ASSERTIONS & API TESTING
    # =========================================================================
    story.append(Paragraph("5.2 Writing Parallel-Ready Test Classes", h2_style))
    story.append(Paragraph("All test classes MUST inherit from <code>BaseTest</code>. This ensures the full browser thread allocation, context initialization, screenshot-on-failure capture, and reporting nodes are handled transparently. Use custom TestNG groupings to classify runs:", body_style))
    
    test_code = """package com.framework.tests.sample;

import com.framework.annotations.TestGroups;
import com.framework.pages.AdminPage;
import com.framework.pages.LoginPage;
import com.framework.utils.Assertion;
import com.framework.utils.WaitUtil;
import org.testng.annotations.Test;

public class AdminTests extends com.framework.tests.BaseTest {

    @Test(description = "Verify system users search works in Admin module", 
          groups = { TestGroups.REGRESSION, TestGroups.SMOKE })
    public void testAdminUserSearch() {
        // 1. Fluent layout execution
        new LoginPage()
            .open()
            .loginWithConfigCredentials();
            
        AdminPage adminPage = new AdminPage();
        adminPage.navigateToAdminMenu()
                 .searchForUser("Admin");

        // 2. Validate using the custom framework Assertion utility
        String URL = adminPage.getCurrentPageUrl();
        Assertion.assertContains(URL, "admin/viewSystemUsers", "Verify loaded Admin URL");
    }
}"""
    story.append(make_code_block(test_code, body_style))
    
    story.append(Paragraph("5.3 Dynamic Database Assertions", h2_style))
    story.append(Paragraph("The framework contains a JDBC database utility (`DBUtil`) that connects to database layers defined in properties. Use this to perform back-end data consistency assertions directly in tests:", body_style))
    
    db_code = """// Fetch dynamic booking state in one step
List<Map<String, String>> dbResults = DBUtil.executeQuery(
    "SELECT status, total_price FROM bookings WHERE guest_name = ?", "Alice Patel"
);

Assertion.assertEquals(dbResults.size(), 1, "Verify exactly one database booking record");
Assertion.assertEquals(dbResults.get(0).get("status"), "CONFIRMED", "Verify booking status is CONFIRMED");"""
    story.append(make_code_block(db_code, body_style))
    
    story.append(Paragraph("5.4 Lightweight API Verification", h2_style))
    story.append(Paragraph("API steps are seamlessly coordinated with the `RestClient` utility, which is built on **RestAssured**. This is extremely powerful for performing rapid backend setups (e.g. generating dynamic auth tokens) before executing browser UI flows:", body_style))
    
    api_code = """// Perform a POST API call using our RestClient utility
Response res = new RestClient()
    .setContentType("application/json")
    .setBody("{\\"username\\":\\"admin\\", \\"password\\":\\"admin123\\"}")
    .post("/api/v1/auth/login");

String token = res.jsonPath().getString("access_token");
TestContext.put("authToken", token); // Save in our thread-local TestContext
LogUtils.info("Dynamic Token stored: " + token);"""
    story.append(make_code_block(api_code, body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 9: CONTINUOUS INTEGRATION & DELIVERY DEPLOYMENT
    # =========================================================================
    story.append(Paragraph("6. Continuous Integration &amp; Deployment Pipelines", h1_style))
    story.append(Paragraph("Automation suites are designed to be fully execution-safe in head-free runner instances. The repository includes fully functional pipeline files for GitHub Actions and Azure DevOps to facilitate automated pull request builds and nightly regression scheduling.", body_style))
    
    story.append(Paragraph("6.1 GitHub Actions Workflow", h2_style))
    story.append(Paragraph("The GHA workflow is located at `.github/workflows/automation.yml`. It handles JDK setup, gradle caching, downloading the required isolated browsers, test suite execution, and report artifact uploading:", body_style))
    
    gha_code = """name: End-to-End Test Automation Suite

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * *' # Execute nightly at 2:00 AM UTC

jobs:
  automation-run:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Setup Java Development Kit 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'zulu'
          cache: 'gradle'

      - name: Install Playwright Browsers
        run: ./gradlew installPlaywright

      - name: Execute QA Automation Suite (Headless Chrome)
        run: ./gradlew test -Denv=qa -Dbrowser=chrome -Dheadless=true

      - name: Upload Test Report Artifact
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: Extent-Reports-Artifact
          path: reports/extent/"""
    story.append(make_code_block(gha_code, body_style))
    
    story.append(Paragraph("6.2 Azure DevOps Multi-Stage Execution", h2_style))
    story.append(Paragraph("The Azure DevOps pipeline located in `pipeline/azure-pipelines.yml` operates on a structured multi-stage execution model. It enforces an initial **Smoke Gate Stage** to verify high-criticality services. If the smoke tests pass, it automatically triggers the high-concurrency **Regression Testing Stage** in parallel agents:", body_style))
    
    az_code = """trigger:
  - main

stages:
  - stage: SmokeTestingGate
    displayName: "Execute Critical Smoke Suite"
    jobs:
      - job: RunSmoke
        steps:
          - task: Gradle@3
            inputs:
              gradleWrapperFile: 'gradlew'
              tasks: 'test'
              options: '-Denv=qa -Dbrowser=chrome -Dheadless=true -Dgroups=smoke'

  - stage: RegressionTesting
    displayName: "Parallel Multi-Browser Regression"
    dependsOn: SmokeTestingGate
    condition: succeeded()
    jobs:
      - job: ChromeRegression
        steps:
          - script: ./gradlew test -Denv=qa -Dbrowser=chrome -Dheadless=true -Dgroups=regression"""
    story.append(make_code_block(az_code, body_style))
    story.append(PageBreak())
    
    # =========================================================================
    # PAGE 10: DESIGN GUIDELINES & QUALITY BEST PRACTICES
    # =========================================================================
    story.append(Paragraph("7. Design Guidelines &amp; Quality Best Practices", h1_style))
    story.append(Paragraph("Maintaining high testing standards requires absolute consistency across all engineering teams. Below are the mandatory design rules for writing UI and API scripts within this platform:", body_style))
    
    story.append(Paragraph("7.1 Locator Best Practices", h2_style))
    story.append(Paragraph("<b>• Prefer Playwright User-Facing Locators:</b> Whenever possible, avoid raw XPath and CSS paths. Rely on user-facing locators such as <code>page.getByRole()</code>, <code>page.getByText()</code>, or <code>page.getByLabel()</code>. This ensures tests mimic real human behavior and are highly resilient to structural changes in HTML layouts.", bullet_style))
    
    story.append(Paragraph("<b>• Avoid Indexing Selectors:</b> Refrain from using index selectors like <code>(//input)[3]</code>. These are brittle and fail instantly when an unrelated form field is added to the page. Instead, scope selections under unique parent containers or select by relative text elements (e.g. <code>//div[label[text()='Username']]/input</code>).", bullet_style))
    
    story.append(Paragraph("7.2 Page-State &amp; Context Best Practices", h2_style))
    story.append(Paragraph("<b>• Absolute Page Abstraction:</b> Never expose direct Playwright Page controls inside test files. All element actions, timeouts, double-clicks, and scrollings MUST be performed inside a custom Page Object method. Tests must read like a readable narrative of user steps.", bullet_style))
    
    story.append(Paragraph("<b>• Leverage ThreadLocal Cleanups:</b> Never share static page references. BaseTest is fully structured to shut down the exact page instance, clear the thread's ExtentReport reference, and wipe the thread's <code>TestContext</code> map at the end of every method. Ensure tests are fully self-contained; a failure in Test A must never pollute the state of Test B.", bullet_style))
    
    story.append(Spacer(1, 10))
    story.append(Paragraph("7.3 Core Assertions &amp; Logging Standards", h2_style))
    story.append(Paragraph("<b>• Standardize Framework Assertions:</b> Avoid using raw TestNG Assert. Instead, use our custom <code>com.framework.utils.Assertion</code> class. This wraps standard assertions with auto-logging into both ExtentReports and Log4j2 logs, capturing failure screenshots automatically and formatting errors for quick debugging.", bullet_style))
    
    story.append(Paragraph("<b>• Uniform Action Logging:</b> Every Page Object method must use the `LogUtils` wrapper to print logs (e.g. `LogUtils.info(...)`). This guarantees that both CLI outputs and Extent Reports have identical, highly structured timestamps and execution summaries.", bullet_style))
    
    story.append(Spacer(1, 20))
    story.append(make_callout("By adhering to these architectural standards, the enterprise automation suite remains lightning fast, robust, and highly maintainable for years to come.", "info", body_style))
    
    story.append(Spacer(1, 50))
    story.append(Paragraph("<font size='8.5' color='#64748B'><i>End of Technical Document. © 2026 Enterprise Quality Engineering. All Rights Reserved.</i></font>", ParagraphStyle('EndDoc', parent=body_style, alignment=1)))
    
    # 3. Build Document using Custom Numbered Canvas
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated PDF: {pdf_filename}")

if __name__ == '__main__':
    generate_pdf()
