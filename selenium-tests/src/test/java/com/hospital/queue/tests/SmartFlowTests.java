package com.hospital.queue.tests;

import com.hospital.queue.pages.AdminDashboardPage;
import com.hospital.queue.pages.AdminLayoutPage;
import com.hospital.queue.pages.DepartmentsPage;
import com.hospital.queue.pages.DoctorLayoutPage;
import com.hospital.queue.pages.DoctorQueuePage;
import com.hospital.queue.pages.DoctorsPage;
import com.hospital.queue.pages.GenerateTokenPage;
import com.hospital.queue.pages.LoginPage;
import com.hospital.queue.pages.ReceptionistLayoutPage;
import com.hospital.queue.pages.RegisterPatientPage;
import com.hospital.queue.pages.TrackTokenPage;
import com.hospital.queue.pages.UsersPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * ============================================================
 *  SmartFlowApp -- Full Sequential End-to-End Positive Flow
 * ============================================================
 *
 * Covers the complete real-world hospital queue management
 * workflow across all four roles in the correct production order:
 *
 *   STEP 1  -- Admin logs in
 *   STEP 2  -- Admin creates a Department (Oncology)
 *   STEP 3  -- Admin creates a Doctor under that Department
 *   STEP 4  -- Admin creates a Receptionist account
 *   STEP 5  -- Admin logs out
 *   STEP 6  -- Receptionist logs in
 *   STEP 7  -- Receptionist registers a Patient (Ravi Kumar)
 *   STEP 8  -- Receptionist generates a Token for that Patient
 *   STEP 9  -- Receptionist logs out
 *   STEP 10 -- Doctor logs in and sees the Patient in the queue
 *   STEP 11 -- Doctor calls the Patient (status -> IN_PROGRESS)
 *   STEP 12 -- Doctor completes the consultation (status -> COMPLETED)
 *   STEP 13 -- Patient tracks the token and sees Consultation Completed
 *   STEP 14 -- Doctor logs out
 *
 * Architecture: Page Object Model (com.hospital.queue.pages.*).
 *   Each screen in the SmartFlowApp has a corresponding Page class
 *   that owns its locators and exposes high-level actions; this
 *   test class only orchestrates the flow and runs assertions.
 *
 * Test data: src/test/resources/testdata.properties supplies the
 *   base values; a per-run timestamp (TS) is appended where
 *   uniqueness is required so re-runs never collide on the DB.
 *
 * Prerequisites:
 *   1. Backend running at http://localhost:8081
 *   2. Frontend running at http://localhost:4200
 *   3. Default admin: admin@hospital.com / Admin@123
 *   4. Google Chrome installed
 *
 * Run: cd selenium-tests && mvn test
 */
public class SmartFlowTests {

    // -- Properties loaded from src/test/resources/testdata.properties --------
    private static final Properties PROPS = loadProps();

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = SmartFlowTests.class.getClassLoader()
                .getResourceAsStream("testdata.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "testdata.properties not found on the test classpath");
            }
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load testdata.properties", e);
        }
        return p;
    }

    // -- Application URL ------------------------------------------------------
    private static final String BASE_URL = PROPS.getProperty("app.base.url");

    // -- Default admin credentials (seeded by DataInitializer.java) -----------
    private static final String ADMIN_EMAIL    = PROPS.getProperty("admin.email");
    private static final String ADMIN_PASSWORD = PROPS.getProperty("admin.password");

    // -- Maximum time to wait for any element before failing ------------------
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);

    // -- Timestamp suffix -- makes emails & phone unique on every test run ----
    private static final long TS = System.currentTimeMillis();

    // -- Test data shared across all 14 steps ---------------------------------
    // Department name: the backend strips non-letters to build the token
    // prefix, so we map four digits of TS to letters A..J to keep it unique.
    private static final String DEPT_NAME =
        String.valueOf((char)('A' + (int)(TS % 10))) +
        String.valueOf((char)('A' + (int)((TS / 10) % 10))) +
        String.valueOf((char)('A' + (int)((TS / 100) % 10))) +
        String.valueOf((char)('A' + (int)((TS / 1000) % 10))) +
        PROPS.getProperty("dept.suffix");
    private static final String DEPT_DESCRIPTION      = PROPS.getProperty("dept.description");
    private static final String DOCTOR_NAME           = PROPS.getProperty("doctor.name") + TS;
    private static final String DOCTOR_EMAIL          = PROPS.getProperty("doctor.email.prefix")
                                                        + "." + TS
                                                        + PROPS.getProperty("doctor.email.domain");
    private static final String DOCTOR_PASSWORD       = PROPS.getProperty("doctor.password");
    private static final String DOCTOR_SPECIALIZATION = PROPS.getProperty("doctor.specialization");
    private static final String RECEPT_NAME           = PROPS.getProperty("receptionist.name") + TS;
    private static final String RECEPT_EMAIL          = PROPS.getProperty("receptionist.email.prefix")
                                                        + "." + TS
                                                        + PROPS.getProperty("receptionist.email.domain");
    private static final String RECEPT_PASSWORD       = PROPS.getProperty("receptionist.password");
    private static final String PATIENT_NAME          = PROPS.getProperty("patient.name") + TS;
    private static final String PATIENT_PHONE         = PROPS.getProperty("patient.phone.prefix")
                                                        + (TS % 100000);
    private static final String PATIENT_EMAIL         = PROPS.getProperty("patient.email");
    private static final String PATIENT_AGE           = PROPS.getProperty("patient.age");
    private static final String PATIENT_GENDER        = PROPS.getProperty("patient.gender");

    // -- Token number captured in TC08, used in TC13 --------------------------
    // Static so it survives across test method boundaries
    private static String generatedTokenNumber;

    // -- WebDriver and explicit wait ------------------------------------------
    private WebDriver     driver;
    private WebDriverWait wait;


    // ==========================================================================
    //  LIFECYCLE
    // ==========================================================================

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();   // Auto-downloads ChromeDriver

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        wait   = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }


    // ==========================================================================
    //  TEST STEPS
    // ==========================================================================

    // --------------------------------------------------------------------------
    // STEP 1 -- Admin logs in
    // --------------------------------------------------------------------------
    @Test(priority = 1,
          description = "Admin logs in with valid credentials and lands on the Queue Overview dashboard")
    public void TC01_AdminLogin() {

        new LoginPage(driver, wait).open(BASE_URL).loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        AdminLayoutPage layout = new AdminLayoutPage(driver, wait);
        layout.waitForAdminUrl();

        // Assertion 1 -- Admin navbar brand must say "Admin"
        WebElement brand = layout.waitForBrand();
        Assert.assertTrue(brand.isDisplayed(),
            "Admin navbar brand should be visible after login");

        // Assertion 2 -- Dashboard heading must say "Queue Overview"
        WebElement heading = new AdminDashboardPage(driver, wait).waitForHeading();
        Assert.assertTrue(heading.isDisplayed(),
            "'Queue Overview' heading should appear on the admin dashboard");
    }


    // --------------------------------------------------------------------------
    // STEP 2 -- Admin creates a Department
    // --------------------------------------------------------------------------
    @Test(priority = 2,
          description = "Admin creates the Oncology department")
    public void TC02_AdminCreateDepartment() {

        DepartmentsPage departments =
            new AdminLayoutPage(driver, wait).gotoDepartments().waitUntilLoaded();

        departments.createDepartment(DEPT_NAME, DEPT_DESCRIPTION);

        // Assertion 1 -- Green success alert must appear
        WebElement successMsg = departments.waitForSuccessAlert();
        Assert.assertTrue(successMsg.isDisplayed(),
            "Success alert should appear after department creation");

        // Assertion 2 -- Department name must appear in the table below the form
        departments.waitForRowContaining(DEPT_NAME);
        Assert.assertTrue(driver.getPageSource().contains(DEPT_NAME),
            "'" + DEPT_NAME + "' should appear in the departments table");
    }


    // --------------------------------------------------------------------------
    // STEP 3 -- Admin creates a Doctor
    // --------------------------------------------------------------------------
    @Test(priority = 3,
          description = "Admin creates Dr. John Smith under the Oncology department")
    public void TC03_AdminCreateDoctor() {

        DoctorsPage doctors =
            new AdminLayoutPage(driver, wait).gotoDoctors().waitUntilLoaded();

        doctors.createDoctor(DOCTOR_NAME, DOCTOR_EMAIL, DOCTOR_PASSWORD,
                             DEPT_NAME, DOCTOR_SPECIALIZATION);

        // Assertion 1 -- Green success alert must appear
        WebElement successMsg = doctors.waitForSuccessAlert();
        Assert.assertTrue(successMsg.isDisplayed(),
            "Success alert should appear after doctor creation");

        // Assertion 2 -- Doctor name must appear in the doctors table
        doctors.waitForRowContaining(DOCTOR_NAME);
        Assert.assertTrue(driver.getPageSource().contains(DOCTOR_NAME),
            "'" + DOCTOR_NAME + "' should appear in the doctors table");
    }


    // --------------------------------------------------------------------------
    // STEP 4 -- Admin creates a Receptionist account
    // --------------------------------------------------------------------------
    @Test(priority = 4,
          description = "Admin creates a Receptionist account (Mary Reception) from the Users page")
    public void TC04_AdminCreateReceptionist() {

        UsersPage users =
            new AdminLayoutPage(driver, wait).gotoUsers().waitUntilLoaded();

        users.createUser(RECEPT_NAME, RECEPT_EMAIL, RECEPT_PASSWORD, "Receptionist");

        // Assertion 1 -- Green success alert must appear
        WebElement successMsg = users.waitForSuccessAlert();
        Assert.assertTrue(successMsg.isDisplayed(),
            "Success alert should appear after receptionist creation");

        // Assertion 2 -- Receptionist name must appear in the staff table
        users.waitForRowContaining(RECEPT_NAME);
        Assert.assertTrue(driver.getPageSource().contains(RECEPT_NAME),
            "'" + RECEPT_NAME + "' should appear in the staff table");
    }


    // --------------------------------------------------------------------------
    // STEP 5 -- Admin logs out
    // --------------------------------------------------------------------------
    @Test(priority = 5,
          description = "Admin clicks Logout and is redirected back to the Login page")
    public void TC05_AdminLogout() {

        LoginPage login = new AdminLayoutPage(driver, wait).logout();

        // Assertion 1 -- URL must contain /auth/login
        Assert.assertTrue(driver.getCurrentUrl().contains("/auth/login"),
            "Browser should redirect to /auth/login after admin logout");

        // Assertion 2 -- Email input must be visible again on the login page
        login.emailField();
    }


    // --------------------------------------------------------------------------
    // STEP 6 -- Receptionist logs in
    // --------------------------------------------------------------------------
    @Test(priority = 6,
          description = "Receptionist logs in with the account created by Admin")
    public void TC06_ReceptionistLogin() {

        new LoginPage(driver, wait).open(BASE_URL).loginAs(RECEPT_EMAIL, RECEPT_PASSWORD);

        ReceptionistLayoutPage layout = new ReceptionistLayoutPage(driver, wait);
        layout.waitForReceptionistUrl();

        // Assertion 1 -- Receptionist navbar brand must be visible
        WebElement brand = layout.waitForBrand();
        Assert.assertTrue(brand.isDisplayed(),
            "Receptionist navbar brand should be visible after login");

        // Assertion 2 -- "Register Patient" link must be in the navbar
        WebElement registerLink = layout.waitForRegisterPatientLink();
        Assert.assertTrue(registerLink.isDisplayed(),
            "'Register Patient' nav link should be visible in the receptionist portal");

        // Assertion 3 -- "Generate Token" link must be in the navbar
        WebElement generateLink = layout.waitForGenerateTokenLink();
        Assert.assertTrue(generateLink.isDisplayed(),
            "'Generate Token' nav link should be visible in the receptionist portal");
    }


    // --------------------------------------------------------------------------
    // STEP 7 -- Receptionist registers a Patient
    // --------------------------------------------------------------------------
    @Test(priority = 7,
          description = "Receptionist registers Ravi Kumar via the Register Patient form")
    public void TC07_ReceptionistRegisterPatient() {

        RegisterPatientPage register =
            new ReceptionistLayoutPage(driver, wait).gotoRegisterPatient().waitUntilLoaded();

        register.registerPatient(PATIENT_NAME, PATIENT_PHONE, PATIENT_EMAIL,
                                 PATIENT_AGE, PATIENT_GENDER);

        // Assertion 1 -- Green success alert must appear
        WebElement successMsg = register.waitForSuccessAlert();
        Assert.assertTrue(successMsg.isDisplayed(),
            "Success alert should appear after patient registration");

        // Assertion 2 -- Quick link to Generate Token must appear in the success alert
        WebElement quickLink = register.waitForGenerateTokenQuickLink();
        Assert.assertTrue(quickLink.isDisplayed(),
            "Quick-link to Generate Token should appear inside the success message");
    }


    // --------------------------------------------------------------------------
    // STEP 8 -- Receptionist generates a Token for the Patient
    // --------------------------------------------------------------------------
    @Test(priority = 8,
          description = "Receptionist generates a NORMAL priority token for Ravi Kumar with Dr. John Smith")
    public void TC08_ReceptionistGenerateToken() {

        GenerateTokenPage generateToken =
            new RegisterPatientPage(driver, wait).clickGenerateTokenQuickLink().waitUntilLoaded();

        generateToken.selectPatient(PATIENT_NAME + " (" + PATIENT_PHONE + ")");
        generateToken.selectDepartment(DEPT_NAME);
        generateToken.selectDoctor(DOCTOR_NAME + " (" + DOCTOR_SPECIALIZATION + ")");
        generateToken.selectPriority("Normal");
        generateToken.submit();

        // Assertion -- Token number display element must appear
        WebElement tokenDisplay = generateToken.waitForTokenDisplay();
        Assert.assertTrue(tokenDisplay.isDisplayed(),
            "Generated token number should be displayed on screen");

        // Capture the token number (e.g., "ONCO-20260601-001") for use in TC13
        generatedTokenNumber = generateToken.getTokenNumber();
        Assert.assertFalse(generatedTokenNumber.isEmpty(),
            "Captured token number must not be empty");
    }


    // --------------------------------------------------------------------------
    // STEP 9 -- Receptionist logs out
    // --------------------------------------------------------------------------
    @Test(priority = 9,
          description = "Receptionist clicks Logout and is redirected back to the Login page")
    public void TC09_ReceptionistLogout() {

        LoginPage login = new ReceptionistLayoutPage(driver, wait).logout();

        // Assertion 1 -- URL must contain /auth/login
        Assert.assertTrue(driver.getCurrentUrl().contains("/auth/login"),
            "Browser should redirect to /auth/login after receptionist logout");

        // Assertion 2 -- Login email field must be visible again
        login.emailField();
    }


    // --------------------------------------------------------------------------
    // STEP 10 -- Doctor logs in and sees the Patient in the queue
    // --------------------------------------------------------------------------
    @Test(priority = 10,
          description = "Doctor logs in and sees Ravi Kumar waiting in the queue")
    public void TC10_DoctorLoginAndSeeQueue() {

        new LoginPage(driver, wait).open(BASE_URL).loginAs(DOCTOR_EMAIL, DOCTOR_PASSWORD);

        DoctorLayoutPage layout = new DoctorLayoutPage(driver, wait);
        layout.waitForDoctorUrl();

        // Assertion 1 -- Doctor navbar brand must be visible
        WebElement brand = layout.waitForBrand();
        Assert.assertTrue(brand.isDisplayed(),
            "Doctor navbar brand should be visible after login");

        // Assertion 2 -- "My Queue" heading must appear on the queue page
        DoctorQueuePage queue = new DoctorQueuePage(driver, wait);
        WebElement heading = queue.waitForHeading();
        Assert.assertTrue(heading.isDisplayed(),
            "'My Queue' heading should be visible on the doctor queue page");

        // Assertion 3 -- Ravi Kumar must appear as a row in the waiting queue
        queue.waitForPatientRow(PATIENT_NAME);
        Assert.assertTrue(driver.getPageSource().contains(PATIENT_NAME),
            "'" + PATIENT_NAME + "' should appear in the doctor's waiting queue");
    }


    // --------------------------------------------------------------------------
    // STEP 11 -- Doctor calls the Patient
    // --------------------------------------------------------------------------
    @Test(priority = 11,
          description = "Doctor clicks 'Call First Patient' and Ravi Kumar moves to IN_PROGRESS")
    public void TC11_DoctorCallPatient() {

        DoctorQueuePage queue = new DoctorQueuePage(driver, wait);
        queue.callFirstPatient();

        // Assertion 1 -- Token number must appear in the Current Patient card
        WebElement tokenDisplay = queue.waitForTokenDisplay();
        Assert.assertTrue(tokenDisplay.isDisplayed(),
            "Token number should appear in the Current Patient card after calling");

        // Assertion 2 -- Patient name must be visible in the current patient section
        Assert.assertTrue(driver.getPageSource().contains(PATIENT_NAME),
            "'" + PATIENT_NAME + "' should be shown as the current patient being consulted");

        // Assertion 3 -- "Complete & Done" button must now be visible
        WebElement completeBtn = queue.waitForCompleteButton();
        Assert.assertTrue(completeBtn.isDisplayed(),
            "'Complete & Done' button should be visible when a patient is IN_PROGRESS");
    }


    // --------------------------------------------------------------------------
    // STEP 12 -- Doctor completes the consultation
    // --------------------------------------------------------------------------
    @Test(priority = 12,
          description = "Doctor clicks 'Complete & Done' -- token is COMPLETED and queue is empty")
    public void TC12_DoctorCompleteConsultation() {

        DoctorQueuePage queue = new DoctorQueuePage(driver, wait);
        queue.completeCurrentToken();

        // Assertion 1 -- Empty current-patient message must appear
        WebElement noPatientMsg = queue.waitForNoCurrentPatientMessage();
        Assert.assertTrue(noPatientMsg.isDisplayed(),
            "'No patient is currently in consultation' should appear after completing");

        // Assertion 2 -- Empty waiting queue message must appear
        WebElement emptyQueueMsg = queue.waitForNoWaitingQueueMessage();
        Assert.assertTrue(emptyQueueMsg.isDisplayed(),
            "'No patients waiting in the queue' should appear after all consultations are done");
    }


    // --------------------------------------------------------------------------
    // STEP 13 -- Patient tracks token and sees Consultation Completed
    // --------------------------------------------------------------------------
    @Test(priority = 13,
          description = "Patient enters the token number and sees 'Consultation Completed' status")
    public void TC13_PatientTrackTokenCompleted() {

        // Guard: TC08 must have captured a token number for this test to run.
        Assert.assertNotNull(generatedTokenNumber,
            "TC13 cannot run -- TC08 did not capture a token number (token generation failed)");

        TrackTokenPage track = new TrackTokenPage(driver, wait).open(BASE_URL);
        track.trackToken(generatedTokenNumber);

        // Assertion 1 -- "Consultation Completed" banner must be visible
        WebElement completedMsg = track.waitForCompletedMessage();
        Assert.assertTrue(completedMsg.isDisplayed(),
            "'Consultation Completed' must appear when the token status is COMPLETED");

        // Assertion 2 -- "Thank you for visiting" message must also appear
        WebElement thankYouMsg = track.waitForThankYouMessage();
        Assert.assertTrue(thankYouMsg.isDisplayed(),
            "'Thank you for visiting' message should appear below the completed banner");
    }


    // --------------------------------------------------------------------------
    // STEP 14 -- Doctor logs out
    // --------------------------------------------------------------------------
    @Test(priority = 14,
          description = "Doctor clicks Logout -- the complete end-to-end flow is finished")
    public void TC14_DoctorLogout() {

        DoctorLayoutPage layout = new DoctorLayoutPage(driver, wait);
        layout.openQueue(BASE_URL);

        LoginPage login = layout.logout();

        // Assertion 1 -- URL must contain /auth/login
        Assert.assertTrue(driver.getCurrentUrl().contains("/auth/login"),
            "Browser should redirect to /auth/login after doctor logout");

        // Assertion 2 -- Login email field must be visible -- confirms the full flow ended cleanly
        WebElement emailField = login.emailField();
        Assert.assertTrue(emailField.isDisplayed(),
            "Login email field should be visible after doctor logout -- full flow complete");
    }
}
