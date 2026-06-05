# SmartFlowApp — Selenium Test Suite Guide & Interview Prep

> One-stop reference: explains every file in `selenium-tests/`, walks through the end-to-end flow, and gives interview-style Q&A you can use to prepare.

---

## 1. What does this test project do?

`selenium-tests/` is an **end-to-end UI automation suite** for SmartFlowApp — a hospital queue-management system. It drives a real Chrome browser through the production Angular frontend (port 4200) which talks to the Spring Boot backend (port 8081).

The suite is split into **two complementary test classes**, both declared in `testng.xml`:

### 1.1 `SmartFlowTests` — Staff-driven flow (14 test cases)

The original real-world hospital workflow across **4 roles** (Admin, Receptionist, Doctor, Patient). A receptionist registers the patient and generates the token on their behalf:

| # | Role | Action |
|---|------|--------|
| TC01 | Admin | Logs in |
| TC02 | Admin | Creates a Department |
| TC03 | Admin | Creates a Doctor |
| TC04 | Admin | Creates a Receptionist |
| TC05 | Admin | Logs out |
| TC06 | Receptionist | Logs in |
| TC07 | Receptionist | Registers a Patient |
| TC08 | Receptionist | Generates a Token |
| TC09 | Receptionist | Logs out |
| TC10 | Doctor | Logs in and sees patient in queue |
| TC11 | Doctor | Calls patient (status → IN_PROGRESS) |
| TC12 | Doctor | Completes consultation (status → COMPLETED) |
| TC13 | Patient | Tracks token, sees "Consultation Completed" |
| TC14 | Doctor | Logs out |

### 1.2 `PatientFlowTests` — Patient self-service flow (9 test cases)

The same end goal but the patient self-registers from the public `/patient` page without a receptionist:

| # | Role | Action |
|---|------|--------|
| TC01 | Admin | Seeds a Department and a Doctor (test setup) |
| TC02 | Admin | Logs out (patient flow runs unauthenticated) |
| TC03 | Patient | Lands on the public `/patient` home page |
| TC04 | Patient | Self-registers (3-step form — step 1: personal details) |
| TC05 | Patient | Picks the department + doctor and receives a Token |
| TC06 | Doctor | Logs in and sees the self-registered patient in the queue |
| TC07 | Doctor | Calls the patient (status → IN_PROGRESS) |
| TC08 | Doctor | Completes the consultation (status → COMPLETED) |
| TC09 | Patient | Tracks the token, sees "Consultation Completed" |

Both test classes are independent — they boot their own `WebDriver`, generate their own unique timestamp-suffixed test data, and tear down cleanly at the end.

---

## 2. Tech stack

| Tool | Version | Role |
|------|---------|------|
| Java | 17 | Language |
| Maven | 3.x | Build tool |
| Selenium WebDriver | 4.18.1 | Browser automation |
| TestNG | 7.9.0 | Test framework (annotations, runner, assertions) |
| WebDriverManager | 5.7.0 | Auto-downloads correct ChromeDriver binary |
| Google Chrome | Latest | Target browser |

**Design patterns used**

- **Page Object Model (POM)** — one class per UI screen, locators private, actions public.
- **Selenium PageFactory** — layout / navbar pages use `@FindBy` field declarations and `PageFactory.initElements(driver, this)` for lazy element initialisation.
- **Shared `BasePage` (utils package)** — every Page Object extends `com.hospital.queue.utils.BasePage`, which centralises explicit-wait helpers, typing, dropdown selection and URL waits.
- **Externalised test data** — base values in a `.properties` file, timestamp appended at runtime for uniqueness.
- **Explicit waits only** — every interaction is gated by `WebDriverWait` (no `Thread.sleep`).

---

## 3. Project structure

```
selenium-tests/
├── pom.xml                              ← Maven build + dependencies
├── testng.xml                           ← TestNG suite definition (two <test> blocks)
├── SELENIUM_TESTS_GUIDE.md              ← (this file)
└── src/test/
    ├── java/com/hospital/queue/
    │   ├── pages/                       ← Page Object Model
    │   │   ├── LoginPage.java
    │   │   ├── AdminLayoutPage.java
    │   │   ├── AdminDashboardPage.java
    │   │   ├── DepartmentsPage.java
    │   │   ├── DoctorsPage.java
    │   │   ├── UsersPage.java
    │   │   ├── ReceptionistLayoutPage.java
    │   │   ├── RegisterPatientPage.java
    │   │   ├── GenerateTokenPage.java
    │   │   ├── DoctorLayoutPage.java
    │   │   ├── DoctorQueuePage.java
    │   │   ├── TrackTokenPage.java
    │   │   ├── PatientHomePage.java          ← NEW (public /patient landing)
    │   │   └── PatientSelfRegisterPage.java  ← NEW (public /patient/register 3-step form)
    │   ├── utils/                       ← Reusable Selenium primitives
    │   │   └── BasePage.java            ← MOVED here from pages/
    │   └── tests/
    │       ├── SmartFlowTests.java      ← 14 staff-driven test cases
    │       └── PatientFlowTests.java    ← NEW: 9 patient self-service test cases
    └── resources/
        └── testdata.properties          ← Externalised test data
```

---

## 4. File-by-file deep dive

### 4.1 `pom.xml`

The Maven Project Object Model — declares Java 17, UTF-8 encoding, three runtime dependencies (Selenium, WebDriverManager, TestNG), and configures the **maven-surefire-plugin** to consume `testng.xml` so `mvn test` runs the suite.

> ⚠️ Don't confuse Maven's `pom.xml` with the **Page Object Model (POM)** design pattern. The names collide but they are different things.

### 4.2 `testng.xml`

TestNG suite descriptor. Declares **two `<test>` blocks** that run sequentially:

1. **`Hospital Queue Management Tests`** → `com.hospital.queue.tests.SmartFlowTests` (the 14-step staff-driven flow, `priority = 1..14`).
2. **`Patient-Perspective Tests`** → `com.hospital.queue.tests.PatientFlowTests` (the 9-step patient self-service flow, `priority = 1..9`).

Each class uses `priority` to enforce the order of methods inside that class. The two classes are otherwise independent — each spins up its own `WebDriver` in `@BeforeClass` and quits it in `@AfterClass`.

### 4.3 `src/test/resources/testdata.properties`

Holds **base** values for every entity the suite creates:

```
app.base.url=http://localhost:4200
admin.email=admin@hospital.com
admin.password=Admin@123
dept.suffix=Dept
doctor.name=Dr. Smith
doctor.email.prefix=dr.smith
doctor.email.domain=@hospital.com
... (etc)
```

The test class reads these once in a static initializer and appends `System.currentTimeMillis()` where uniqueness is required (names, emails, phone). This is what stops re-runs from colliding with old rows in the DB.

### 4.4 `utils/BasePage.java`

> **Note:** This class lives in `com.hospital.queue.utils` (not `pages`) since it is a shared utility, not a page object. Every concrete page in `pages/` extends it.

Abstract parent for every Page Object. Holds the `WebDriver` and `WebDriverWait` references, and exposes **reusable primitives**:

| Method | Purpose |
|--------|---------|
| `waitVisible(By)` / `waitVisible(WebElement)` | Wait until element is visible, return it (overload accepts a PageFactory-managed `WebElement`) |
| `waitClickable(By)` / `waitClickable(WebElement)` | Wait until element is clickable, return it |
| `typeInto(By, text)` / `typeInto(WebElement, text)` | Wait → clear → sendKeys |
| `selectByText(By, text)` | Selenium `Select` wrapper for `<select>` dropdowns |
| `selectFromPopulatedDropdown(By, text)` | Same, but first waits for the dropdown to load >1 option from the API |
| `clickWhenReady(By)` / `clickWhenReady(WebElement)` | Wait clickable → click |
| `waitForUrl(fragment)` | Wait until `urlContains(fragment)` |
| `getCurrentUrl()` / `getPageSource()` | Public passthroughs used by the test classes for assertions |

The `WebElement` overloads exist because some pages (e.g. `AdminLayoutPage`) use Selenium **PageFactory** with `@FindBy` fields — those fields are `WebElement` references, not `By` locators, so the helpers accept both forms.

Every page extends this so the higher layers stay clean.

### 4.5 `pages/LoginPage.java`

Models `/auth/login`. Methods:
- `open(baseUrl)` — navigates and waits for the email input.
- `loginAs(email, password)` — fills both fields and clicks Sign In.
- `emailField()` — returns the input, used as a "we are back on login" check after logout.

### 4.6 `pages/AdminLayoutPage.java`

Models the **navbar and logout button** shared by every `/admin/*` page. This page uses **Selenium PageFactory**:

```java
@FindBy(how = How.XPATH, using = "//nav/a[contains(text(),'Departments')]")
private WebElement departmentsLink;
...
public AdminLayoutPage(WebDriver driver, WebDriverWait wait) {
    super(driver, wait);
    PageFactory.initElements(driver, this);   // lazy proxy initialisation
}
```

Returning a strongly-typed next page is the POM idiom:

- `gotoDepartments()` → `DepartmentsPage`
- `gotoDoctors()` → `DoctorsPage`
- `gotoUsers()` → `UsersPage`
- `logout()` → `LoginPage`
- `waitForBrand()` — verifies the "Admin" brand label is rendered.
- `waitForAdminUrl()` — explicit URL check after login.

### 4.7 `pages/AdminDashboardPage.java`

The `/admin` landing page — only assertion target is the **"Queue Overview"** heading.

### 4.8 `pages/DepartmentsPage.java` / `DoctorsPage.java` / `UsersPage.java`

Three Admin form pages with the same shape:
- `waitUntilLoaded()` — wait for the page heading.
- A single `createXxx(...)` action method that fills every field and clicks the submit button.
- `waitForSuccessAlert()` and `waitForRowContaining(text)` — verify the green alert appears and the new entity shows up in the table below the form.

`DoctorsPage.createDoctor(...)` uses `selectFromPopulatedDropdown` because the department dropdown loads asynchronously from the API.

### 4.9 `pages/ReceptionistLayoutPage.java`

Equivalent of `AdminLayoutPage` for `/receptionist/*`:
- `gotoRegisterPatient()` → `RegisterPatientPage`
- `logout()` → `LoginPage`
- `waitForBrand()`, `waitForRegisterPatientLink()`, `waitForGenerateTokenLink()` — used by TC06 to verify the post-login portal renders the right shell.

### 4.10 `pages/RegisterPatientPage.java`

Form at `/receptionist/register`. The interesting method is `clickGenerateTokenQuickLink()` — the success alert contains an inline link to the Generate Token page; clicking it returns a typed `GenerateTokenPage`, just like Admin layout's `gotoXxx()` methods.

### 4.11 `pages/GenerateTokenPage.java`

Four cascading `<select>` dropdowns at `/receptionist/generate-token`. Because options load asynchronously (patient list, dept list, doctors filtered by dept), each `selectXxx` uses `selectFromPopulatedDropdown`. After `submit()`, `getTokenNumber()` reads the displayed token (e.g. `JICG-20260604-001`) for the patient-tracking step.

### 4.12 `pages/DoctorLayoutPage.java`

Doctor portal shell. `openQueue(baseUrl)` directly navigates to `/doctor/queue` (used by TC14 because TC13 ended on the patient track page). `logout()` returns `LoginPage`.

### 4.13 `pages/DoctorQueuePage.java`

The real work of TC11 / TC12:
- `callFirstPatient()` — clicks "Call First Patient".
- `completeCurrentToken()` — clicks "Complete & Done".
- Assertion helpers: `waitForHeading()`, `waitForPatientRow(name)`, `waitForTokenDisplay()`, `waitForCompleteButton()`, `waitForNoCurrentPatientMessage()`, `waitForNoWaitingQueueMessage()`.

### 4.14 `pages/TrackTokenPage.java`

Public page at `/patient/track` — no login. `trackToken(num)` fills the input and clicks Track; `waitForCompletedMessage()` / `waitForThankYouMessage()` validate the green completion banner.

### 4.15 `pages/PatientHomePage.java`

Public landing page at `/patient` — no login required. Used as the patient self-service entry point.

- `open(baseUrl)` — navigates to `/patient` and waits for the "Smart Hospital" heading.
- `waitForHeading()` — returns the visible `<h1>` for assertions.
- `clickRegister()` → `PatientSelfRegisterPage` — follows the "Register & Get Token" link.
- `clickTrack()` → `TrackTokenPage` — follows the "Track Your Token" link.
- `PATH` — public constant (`"/patient"`) so test classes can assert on the URL without hardcoding strings.

### 4.16 `pages/PatientSelfRegisterPage.java`

The **three-step** public form at `/patient/register`. No login required.

| Step | Action | Method |
|------|--------|--------|
| 1 | Patient enters name, phone, email, age, gender → clicks **Continue** | `fillPatientDetails(name, phone, email, age, gender)` |
| 2 | Patient picks Department then Doctor (both dropdowns load from the API) → clicks **Get My Token** | `pickDoctorAndGetToken(deptName, doctorOptionText)` |
| 3 | Generated token number is shown | `waitForTokenDisplay()` / `getTokenNumber()` / `waitForTrackLink()` |

Both step-2 dropdowns use `selectFromPopulatedDropdown` because their options are populated asynchronously from a REST call. The doctor option text follows the format `"<doctor name> — <specialization>"`.

### 4.17 `tests/SmartFlowTests.java`

The staff-driven flow orchestrator. Responsibilities:

1. **Lifecycle** — `@BeforeClass setUp()` boots ChromeDriver via WebDriverManager and creates the 20-second `WebDriverWait`; `@AfterClass tearDown()` quits the driver.
2. **Test data setup** — static initializer loads `testdata.properties`, builds unique values by appending `TS = System.currentTimeMillis()`.
3. **14 test cases** — each annotated with `@Test(priority = N, description = "...")`. Each consists of a few page-object calls followed by `Assert.assertTrue(...)` checks. The test class contains **no Selenium primitives** any more — only page objects and assertions.

Token continuity: TC08 stores the generated token in a `static String generatedTokenNumber`; TC13 reads it. Static so it survives across method boundaries within a single suite run.

### 4.18 `tests/PatientFlowTests.java`

The **patient-perspective** orchestrator. Same lifecycle and test-data conventions as `SmartFlowTests`, but the storyline runs from the patient's point of view:

1. **TC01–TC02** — Admin logs in, seeds a Department + Doctor (because the backend's `DataInitializer` only seeds the default admin, not the data the patient will pick), then logs out so the rest of the flow is anonymous.
2. **TC03** — Patient opens the public `/patient` home page (no auth).
3. **TC04** — Patient fills personal details on the 3-step `/patient/register` form (step 1).
4. **TC05** — Patient selects the seeded department + doctor and gets a token. The token number is captured in `static String generatedTokenNumber`.
5. **TC06–TC08** — Doctor logs in as the freshly-created account, sees the self-registered patient in the queue, calls them, completes the consultation.
6. **TC09** — Patient opens `/patient/track`, enters the captured token, and asserts the "Consultation Completed" banner is visible.

Both test classes share the same `testdata.properties` keys and the same 4-letter-prefix trick for the department name.

---

## 5. End-to-end flow — step by step

### 5.1 `SmartFlowTests` — staff-driven flow (14 TCs)

```
┌──────────────────────────────────────────────────────────────────┐
│  ADMIN PHASE                                                     │
├──────────────────────────────────────────────────────────────────┤
│  TC01  Login  ──►  AdminDashboard (Queue Overview)               │
│  TC02  → /admin/departments  → create Dept → assert table row    │
│  TC03  → /admin/doctors      → create Doctor → assert table row  │
│  TC04  → /admin/users        → create Receptionist               │
│  TC05  Logout  ──►  Login                                        │
├──────────────────────────────────────────────────────────────────┤
│  RECEPTIONIST PHASE                                              │
├──────────────────────────────────────────────────────────────────┤
│  TC06  Login (as receptionist)  ──► Receptionist portal          │
│  TC07  → /receptionist/register       → register patient         │
│  TC08  → /receptionist/generate-token → 4 dropdowns → submit     │
│         capture token (e.g. JICG-20260604-001)                   │
│  TC09  Logout  ──►  Login                                        │
├──────────────────────────────────────────────────────────────────┤
│  DOCTOR PHASE                                                    │
├──────────────────────────────────────────────────────────────────┤
│  TC10  Login (as doctor)  ──►  /doctor/queue (sees patient)      │
│  TC11  Click "Call First Patient"  → IN_PROGRESS                 │
│  TC12  Click "Complete & Done"     → COMPLETED, queue empty      │
├──────────────────────────────────────────────────────────────────┤
│  PATIENT PHASE                                                   │
├──────────────────────────────────────────────────────────────────┤
│  TC13  /patient/track  → enter token → "Consultation Completed"  │
│  TC14  Doctor logout                                             │
└──────────────────────────────────────────────────────────────────┘
```

### 5.2 `PatientFlowTests` — patient self-service flow (9 TCs)

```
┌──────────────────────────────────────────────────────────────────┐
│  SETUP (Admin)                                                   │
├──────────────────────────────────────────────────────────────────┤
│  TC01  Admin login → create Dept + Doctor (needed for the pick)  │
│  TC02  Admin logout — rest of the flow is anonymous              │
├──────────────────────────────────────────────────────────────────┤
│  PATIENT SELF-SERVICE (public, no login)                         │
├──────────────────────────────────────────────────────────────────┤
│  TC03  Open /patient → assert "Smart Hospital" heading           │
│  TC04  Click "Register & Get Token" → fill step-1 personal data  │
│  TC05  Pick Dept + Doctor → "Get My Token" → capture token       │
├──────────────────────────────────────────────────────────────────┤
│  DOCTOR HANDLES THE TOKEN                                        │
├──────────────────────────────────────────────────────────────────┤
│  TC06  Doctor login → sees self-registered patient in queue      │
│  TC07  Click "Call First Patient"  → IN_PROGRESS                 │
│  TC08  Click "Complete & Done"     → COMPLETED, queue empty      │
├──────────────────────────────────────────────────────────────────┤
│  PATIENT VERIFIES                                                │
├──────────────────────────────────────────────────────────────────┤
│  TC09  /patient/track  → enter token → "Consultation Completed"  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 6. Key design decisions explained

### 6.1 Why Page Object Model?

Every UI screen has a class that owns its locators. Tests speak in **business actions** (`departments.createDepartment(name, desc)`) instead of `By.xpath("...")`. Two huge wins:

- **Locator change isolation.** If the "Create Department" button XPath changes, you fix it in *one* line in `DepartmentsPage`, not 14 places in the test class.
- **Readability.** A test reads like a story instead of a wall of `findElement` calls.

### 6.2 Why a properties file for test data?

Hardcoded test data is brittle — change a doctor's name and you edit Java code. With `testdata.properties`:

- Non-developers (QA leads, BAs) can edit data.
- Different environments can swap the file (`testdata.properties` for local, `testdata.qa.properties` for QA).
- The Java side stays clean — it just calls `PROPS.getProperty("doctor.name")`.

### 6.3 Why the timestamp suffix?

The backend enforces unique emails and phone numbers in the DB. Without `TS`, the second run fails because `dr.smith@hospital.com` already exists. With `TS`, every value becomes `dr.smith.1722944193127@hospital.com` — unique per millisecond.

The **department name** is special: the backend strips non-letters to compute the token prefix (`deptName.replaceAll("[^A-Za-z]","").toUpperCase().substring(0,4)`), so a numeric `TS` won't help. The trick is to map four digits of `TS` to letters A..J — that gives a deterministic-per-run **4-letter** unique prefix.

### 6.4 Why explicit waits only?

- `Thread.sleep(3000)` either wastes time (element appears after 200 ms) or fails (element takes 4 s).
- `WebDriverWait.until(ExpectedConditions.visibilityOfElementLocated(...))` polls every ~500 ms and returns the instant the condition is true. Faster *and* more reliable.

### 6.5 Why no `formcontrolname` / `ng-*` selectors? (mostly)

The default strategy uses only **standard HTML attributes**: `@type`, `@placeholder`, `@class`, `text()`. This means the tests would survive a migration off Angular to React/Vue — the underlying HTML structure is the contract.

The one exception is `PatientSelfRegisterPage`, which uses `@formcontrolname` for the public 3-step form. There the inputs have no unique placeholders or labels we can lock onto, and Angular's `formcontrolname` is the most reliable stable selector available. If/when those inputs get unique IDs or `data-test` attributes, those selectors will be the first to migrate.

### 6.6 Why PageFactory in layout pages?

`AdminLayoutPage` (and similarly shaped layout pages) uses Selenium's `@FindBy` + `PageFactory.initElements`. Two reasons:

- **Lazy initialisation.** The proxy WebElement is only resolved when first used — so a layout class that defines 5 nav links doesn't trigger 5 `findElement` calls at construction time.
- **Declarative locators.** The `@FindBy` annotation reads cleaner than a `By` constant for a class that has many similar elements.

The other pages (forms, dialogs, dropdown-heavy screens) stick with plain `By` constants because their interactions are more procedural and benefit from `By` being passed around (e.g. into `selectFromPopulatedDropdown`).

### 6.7 Why two test classes instead of one big sequential script?

The same business flow can begin two ways in the real product:

1. **Staff-driven** — a receptionist registers the walk-in patient and generates the token. Covered by `SmartFlowTests`.
2. **Patient self-service** — a patient walks themselves through `/patient` → `/patient/register` and gets the same token without staff involvement. Covered by `PatientFlowTests`.

Both paths must work, and they exercise different page objects (`RegisterPatientPage` + `GenerateTokenPage` vs. `PatientHomePage` + `PatientSelfRegisterPage`). Splitting into two TestNG `<test>` blocks keeps each scenario readable end-to-end and lets the patient flow stand alone in CI if the staff flow is temporarily broken.

---

## 7. How to run

```bash
# from repo root
cd selenium-tests
mvn clean test
```

Prerequisites:
1. Backend up at `http://localhost:8081`.
2. Frontend up at `http://localhost:4200`.
3. Default admin (`admin@hospital.com` / `Admin@123`) seeded by `DataInitializer.java`.
4. Google Chrome installed (WebDriverManager downloads the matching ChromeDriver automatically).

---

## 8. Interview-style Q&A

> Tip: answer in **STAR-lite form** for project-specific questions — what the project does, what you built, what tools you used, what the outcome was. For fundamentals, lead with a one-line definition, then a concrete example.

### 8.1 Selenium fundamentals

**Q1. What is Selenium WebDriver?**
A: An open-source library that lets your code drive a real browser. It speaks the W3C WebDriver protocol to a browser-specific driver (ChromeDriver, geckodriver) which controls the actual browser process.

**Q2. Selenium IDE vs Selenium WebDriver vs Selenium Grid?**
A: IDE is the record-and-playback Chrome extension (good for quick smoke tests). WebDriver is the programmatic API you call from Java/Python/etc. — that's what this project uses. Grid lets you distribute the same tests across many machines / browsers in parallel.

**Q3. How does WebDriver actually click an element?**
A: Selenium sends an HTTP request (W3C protocol) to the browser driver process. The driver translates it into a browser-native command that synthesises a real mouse event on the element's coordinates.

**Q4. What's WebDriverManager and why do we use it?**
A: A library that downloads the correct ChromeDriver binary for the installed Chrome version at runtime. Without it you'd manually download `chromedriver.exe`, put it on PATH, and update it every time Chrome auto-updates. We call `WebDriverManager.chromedriver().setup()` once in `@BeforeClass`.

### 8.2 Locators

**Q5. Which locators does Selenium support?**
A: `id`, `name`, `className`, `tagName`, `linkText`, `partialLinkText`, `cssSelector`, `xpath`.

**Q6. Which locators do you prefer and why?**
A: I prefer `id` when available (fast, unique). If not, I prefer a stable CSS selector or an XPath built on **standard HTML attributes** (`type`, `placeholder`, visible text). In this project I avoided framework attributes like `formcontrolname` so the tests would survive a UI library migration.

**Q7. XPath vs CSS — which is faster?**
A: CSS is usually faster in modern browsers because it's evaluated natively. But XPath is more expressive — only XPath can match by visible `text()`, walk up the DOM with `parent::`, or do `contains()` on text. I use XPath in this project mainly for `contains(text(), ...)` on form labels and table cells.

**Q8. What's a good vs bad XPath?**
A: Bad: `/html/body/div[3]/div/form/div[2]/input[1]` — brittle, breaks on any DOM change.
Good: `//input[@placeholder='Patient full name']` — semantic, survives layout reshuffles.

**Q9. How do you find an element whose label text is "Email" but you need the input next to it?**
A: `//label[text()='Email']/following-sibling::input` — XPath axes are perfect for this.

### 8.3 Waits

**Q10. Three types of Selenium waits?**
A:
- **Implicit wait** — global; WebDriver polls for the element up to the timeout. Set once on the driver.
- **Explicit wait** (`WebDriverWait`) — condition-specific; waits for `visibilityOf`, `elementToBeClickable`, etc.
- **Fluent wait** — explicit wait with custom polling interval and ignored exceptions.

**Q11. Why don't you mix implicit and explicit waits?**
A: They compound unpredictably — a 10-second implicit + 20-second explicit can stretch to 30+ seconds, or interact in ways that hide failures. This project uses **only explicit waits**.

**Q12. What's the difference between `presenceOfElementLocated` and `visibilityOfElementLocated`?**
A: `presence` returns as soon as the element exists in the DOM, even if `display:none`. `visibility` additionally requires the element to be displayed (non-zero size, not hidden). For user-facing interactions I always use `visibility`.

**Q13. How would you wait for a dropdown to finish loading its options from an API call?**
A: Custom lambda in `WebDriverWait.until`:
```java
wait.until(d -> new Select(el).getOptions().size() > 1);
```
That's exactly what `BasePage.selectFromPopulatedDropdown` does in this project — the dropdown starts with just the placeholder option, then the AJAX response populates the rest.

**Q14. Why is `Thread.sleep` bad?**
A: It always blocks for the full duration regardless of when the element is ready. Explicit waits return the moment the condition becomes true — faster on the happy path, more reliable when latency spikes.

### 8.4 Page Object Model (POM)

**Q15. What is POM?**
A: A design pattern where each UI screen has a corresponding class. The class owns its locators (private) and exposes high-level actions (public). Tests interact only with the action methods, never with `By` locators directly.

**Q16. What are the benefits?**
A:
- **Maintainability** — locator change touches one class.
- **Readability** — tests read like business scenarios.
- **Reusability** — `loginAs(email, pw)` works for admin, doctor, receptionist.

**Q17. Should page objects contain assertions?**
A: Strong opinion: no. Pages expose state via getters / wait-helpers; the test class owns the assertions. That keeps pages reusable across positive *and* negative tests. (This project follows that rule — assertions live only in `SmartFlowTests.java`.)

**Q18. What about a fluent / builder POM where each method returns `this`?**
A: Useful for chained actions on the same page (`departments.fillName(n).fillDesc(d).submit()`). For navigation, returning the **next page** type is more powerful — IDE autocompletion shows you only valid next steps. This project uses both: `register.waitUntilLoaded()` returns `this`, `layout.gotoDepartments()` returns `DepartmentsPage`.

**Q19. Where do you put common helpers like "wait & click"?**
A: In a `BasePage` abstract class that every page extends. In this project it has `waitVisible`, `typeInto`, `selectByText`, `selectFromPopulatedDropdown`, `clickWhenReady`, `waitForUrl`.

### 8.5 TestNG

**Q20. TestNG vs JUnit?**
A: TestNG predates JUnit 5 with features now in both: parameterised tests, parallel execution, suite descriptors. TestNG still has stronger built-in **test ordering** (`priority`, `dependsOnMethods`) and grouping (`groups = {...}`). I chose TestNG here because the 14 tests must run in a fixed order.

**Q21. Key TestNG annotations?**
A:
- `@BeforeSuite` / `@AfterSuite` — once per suite.
- `@BeforeClass` / `@AfterClass` — once per class (we use these for driver setup/teardown).
- `@BeforeMethod` / `@AfterMethod` — around each `@Test`.
- `@Test` — marks a test method.
- `@DataProvider` — feeds parameterised tests.

**Q22. How do you enforce test execution order in TestNG?**
A: Two ways: `priority = N` (lower runs first) or `dependsOnMethods = {"TC07_..."}`. This project uses `priority`. `dependsOnMethods` would also **skip** dependent tests when an upstream one fails — useful if you want to short-circuit later steps.

**Q23. What does `testng.xml` do?**
A: Declares the test suite — which classes / packages / methods to run, with what parameters, in parallel or sequentially. Surefire reads it via the plugin configuration in `pom.xml`.

**Q24. How would you run tests in parallel?**
A: In `testng.xml`: `<suite name="..." parallel="classes" thread-count="4">`. But you also need thread-safe state — each test class would need its own WebDriver instance (typically a `ThreadLocal<WebDriver>`).

### 8.6 Test data, configuration, properties

**Q25. Why externalise test data into `.properties`?**
A: Decouples *what* the test does from *what data* it uses. QA can edit data without touching Java; you can swap files per environment; sensitive values can move to a vault later without code changes.

**Q26. How do you load a properties file at runtime?**
A:
```java
Properties p = new Properties();
try (InputStream in = getClass().getClassLoader()
        .getResourceAsStream("testdata.properties")) {
    p.load(in);
}
```
Loading via `ClassLoader.getResourceAsStream` reads from the test classpath (`src/test/resources`), so the file ships inside the JAR / test-classes folder rather than being read from a hardcoded disk path.

**Q27. How do you avoid "duplicate entry" DB errors when re-running tests?**
A: Append `System.currentTimeMillis()` to every unique field (email, phone, name). This project does that for doctor/receptionist/patient. For the department name, a numeric timestamp gets stripped by the backend's token-prefix logic, so we map TS digits to letters (`0→A … 9→J`) and use a 4-letter alphabetic prefix.

### 8.7 Build & CI

**Q28. How do you run the suite from the command line?**
A: `mvn clean test` from `selenium-tests/`. Maven's Surefire plugin reads `testng.xml` (configured in `pom.xml`) and runs everything.

**Q29. How would you run this in CI (Jenkins, GitHub Actions)?**
A: Use headless Chrome (`options.addArguments("--headless=new")`) on the agent, archive the Surefire HTML reports as artifacts, fail the build on any test failure. WebDriverManager handles ChromeDriver download on the agent automatically.

**Q30. Where does Surefire put the reports?**
A: `target/surefire-reports/` — both XML (machine-readable, for CI) and HTML (human-readable).

### 8.8 This project specifically

**Q31. Walk me through your test project.**
A: "I built a full end-to-end Selenium suite for SmartFlowApp, a hospital queue management system with four user roles. The suite is split into two TestNG classes that cover the same business flow from two angles — a 14-step staff-driven scenario where a receptionist creates the token, and a 9-step patient self-service scenario where the patient self-registers from the public `/patient` page. I used **Page Object Model** with 14 page classes plus a shared `BasePage` in a separate `utils` package. Layout pages use Selenium **PageFactory** with `@FindBy` for lazy element initialisation. All test data lives in a `.properties` file and every unique field is suffixed with a millisecond timestamp so re-runs never collide on the database. Built with Maven, TestNG, and WebDriverManager so ChromeDriver is provisioned automatically."

**Q32. Why one big sequential test instead of 14 independent ones?**
A: The scenario *is* sequential — a doctor can't see a patient in their queue until the receptionist has generated a token, and that requires the doctor to exist, which requires the department to exist. The realistic, production-like flow surfaces integration issues that 14 mocked unit tests would miss. The trade-off is that an early failure cascades — but that's exactly the signal you want when a foundational step breaks.

**Q33. How does your design handle the dropdown that loads from an API?**
A: `BasePage.selectFromPopulatedDropdown` uses a custom `WebDriverWait` lambda to wait until `Select.getOptions().size() > 1` — meaning the placeholder option is no longer alone, i.e. the API response has populated the list. This is more reliable than a fixed sleep.

**Q34. How would you scale this from 14 tests to 140?**
A: Split into independent classes by role (`AdminTests`, `ReceptionistTests`, `DoctorTests`, `PatientTests`), enable TestNG parallel-by-class with one WebDriver per thread, and front-load test data via API calls (using REST Assured) instead of UI clicks where possible — UI tests should validate UI behaviour, not seed data.

**Q35. What would you add if you had more time?**
A:
- **REST Assured** for API-level setup and teardown (faster, more reliable than UI seeding).
- **Allure** or **ExtentReports** for richer HTML reporting.
- **Cross-browser**: Firefox and Edge via a `@Parameters("browser")` factory.
- **Headless mode** as a Maven profile for CI.
- **Screenshot-on-failure** via a TestNG listener.
- **Environment configs** — `testdata.dev.properties`, `testdata.qa.properties`, picked via `-Denv=qa`.

**Q36. How do you debug a flaky test?**
A: Step 1, reproduce locally with the same browser version. Step 2, add `System.out.println` or attach a debugger to find the exact wait that times out. Step 3, check whether the failure is timing (need a better wait) or state (data from a previous run polluted the DB) or environment (slow CI agent). The timestamp-based unique data in this project removes a whole category of state flakiness.

**Q37. What does `static String generatedTokenNumber` do in your test class?**
A: TC08 captures the token from the Generate Token page; TC13 needs it to look up the same token on the patient track page. Because TestNG instantiates the class once and runs all methods on it, a static field survives across method boundaries within the suite run. (An instance field would also work — `static` here is just emphasising that it's shared cross-method state.)

**Q38. What's the difference between your `pom.xml` and the POM in "Page Object Model"?**
A: Total coincidence of naming. `pom.xml` is the Maven Project Object Model file (build configuration). POM the design pattern is Page Object Model (test architecture). I always clarify which one I mean — interviewers like to test that.

**Q39. How is your `BasePage` different from a `Utils` class?**
A: `BasePage` is an **abstract base class** that page objects *extend*. It has access to the same `WebDriver` and `WebDriverWait` instance as the subclass. A `Utils` class would be `static` and require passing the driver to every call. Inheritance gives me cleaner subclass code: `typeInto(EMAIL_FIELD, email)` instead of `Utils.typeInto(driver, wait, EMAIL_FIELD, email)`.

**Q40. If I changed the "Create Doctor" button text to "Save Doctor", which files would you edit?**
A: One file — `DoctorsPage.java`, the `CREATE_BUTTON` locator constant. The test class wouldn't change because it only knows about `doctors.createDoctor(...)`. **That's the whole value of POM in one sentence.**

---

## 9. Quick reference — keywords / one-liners for the interview

| Term | One-liner |
|------|-----------|
| Page Object Model | One class per screen; locators private; actions public. |
| Explicit wait | `WebDriverWait` + `ExpectedConditions.X` — waits for a specific condition. |
| WebDriverManager | Auto-downloads the matching browser driver binary. |
| Surefire | Maven plugin that executes tests; reads `testng.xml`. |
| TestNG `priority` | Lower number runs first; for ordered scenarios. |
| `ExpectedConditions` | Library of pre-built waiting conditions (visibility, clickable, urlContains, ...). |
| `Select` | Selenium wrapper for native HTML `<select>` dropdowns. |
| `By` locator | Strategy object: `By.id`, `By.xpath`, `By.cssSelector`, etc. |
| Headless Chrome | Browser runs without a window — used in CI. |
| Implicit wait | Driver-wide default polling for `findElement`; avoid mixing with explicit. |

---

## 10. Sentence-by-sentence "elevator pitch"

> *"SmartFlowApp's UI is automated end-to-end with Selenium WebDriver, TestNG and Maven. I structured the suite using the Page Object Model — fourteen page classes plus a shared `BasePage` in a dedicated `utils` package — so locators and Selenium primitives never leak into the test code. Layout pages use Selenium PageFactory with `@FindBy` for lazy element initialisation. All test data lives in a `.properties` file and is made unique per run with a millisecond timestamp, which means the same suite can run hundreds of times against the same database without duplicate-key failures. There are two test classes: a 14-step staff-driven scenario in `SmartFlowTests` and a 9-step patient self-service scenario in `PatientFlowTests`, both running sequentially via TestNG priorities through admin setup, registration, doctor consultation, and the public patient token-tracking page."*

Use that, then dig deeper based on which thread the interviewer pulls on. Good luck!
