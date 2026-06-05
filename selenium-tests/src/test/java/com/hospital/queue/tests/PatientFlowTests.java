package com.hospital.queue.tests;

import com.hospital.queue.pages.AdminLayoutPage;
import com.hospital.queue.pages.DepartmentsPage;
import com.hospital.queue.pages.DoctorLayoutPage;
import com.hospital.queue.pages.DoctorQueuePage;
import com.hospital.queue.pages.DoctorsPage;
import com.hospital.queue.pages.LoginPage;
import com.hospital.queue.pages.PatientHomePage;
import com.hospital.queue.pages.PatientSelfRegisterPage;
import com.hospital.queue.pages.TrackTokenPage;
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
 *  SmartFlowApp -- Patient-Perspective End-to-End Flow
 * ============================================================
 *
 * This suite tells the story from the patient's point of view:
 * a walk-in patient self-registers online, picks a doctor, gets
 * a token, then a doctor handles them and the patient sees the
 * "Consultation Completed" status on the public tracker.
 *
 *   STEP 1 -- Admin seeds a Department and a Doctor (test setup)
 *   STEP 2 -- Admin logs out so the patient flow starts unauthenticated
 *   STEP 3 -- Patient lands on the public /patient home page
 *   STEP 4 -- Patient self-registers and provides personal details
 *   STEP 5 -- Patient picks the seeded Department + Doctor and gets a Token
 *   STEP 6 -- Doctor logs in and sees the self-registered patient in the queue
 *   STEP 7 -- Doctor calls the patient (status -> IN_PROGRESS)
 *   STEP 8 -- Doctor completes the consultation (status -> COMPLETED)
 *   STEP 9 -- Patient tracks the token and sees "Consultation Completed"
 *
 * Notes:
 *   * The patient self-register screen (/patient/register) is a public
 *     three-step form -- no login required for the patient.
 *   * Admin setup in STEP 1 is unavoidable because the backend's
 *     DataInitializer only seeds the default admin -- not the
 *     department / doctor the patient will pick.
 *   * Test data values are read from testdata.properties and a
 *     timestamp suffix is appended so re-runs never collide on the DB.
 *
 * Prerequisites:
 *   1. Backend running at http://localhost:8081
 *   2. Frontend running at http://localhost:4200
 *   3. Default admin: admin@hospital.com / Admin@123
 *   4. Google Chrome installed
 *
 * Run: cd selenium-tests && mvn test
 */
public class PatientFlowTests {

    // -- Properties loaded from src/test/resources/testdata.properties --------
    private static final Properties PROPS = loadProps();

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = PatientFlowTests.class.getClassLoader()
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

    // -- Wait timeout ---------------------------------------------------------
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);

    // -- Timestamp suffix -- keeps every run unique ---------------------------
    private static final long TS = System.currentTimeMillis();

    // -- Department: 4-letter unique prefix + suffix from properties ----------
    // (backend strips non-letters to build the token prefix, so we map
    //  four digits of TS to letters A..J to keep it unique per run.)
    private static final String DEPT_NAME =
        String.valueOf((char)('A' + (int)(TS % 10))) +
        String.valueOf((char)('A' + (int)((TS / 10) % 10))) +
        String.valueOf((char)('A' + (int)((TS / 100) % 10))) +
        String.valueOf((char)('A' + (int)((TS / 1000) % 10))) +
        PROPS.getProperty("dept.suffix");
    private static final String DEPT_DESCRIPTION      = PROPS.getProperty("dept.description");

    // -- Doctor (will be created during setup, then logged in as) -------------
    private static final String DOCTOR_NAME           = PROPS.getProperty("doctor.name") + TS;
    private static final String DOCTOR_EMAIL          = PROPS.getProperty("doctor.email.prefix")
                                                        + "." + TS
                                                        + PROPS.getProperty("doctor.email.domain");
    private static final String DOCTOR_PASSWORD       = PROPS.getProperty("doctor.password");
    private static final String DOCTOR_SPECIALIZATION = PROPS.getProperty("doctor.specialization");

    // -- Patient (self-registers themselves) ----------------------------------
    private static final String PATIENT_NAME    = PROPS.getProperty("patient.name") + TS;
    private static final String PATIENT_PHONE   = PROPS.getProperty("patient.phone.prefix")
                                                  + (TS % 100000);
    private static final String PATIENT_EMAIL   = PROPS.getProperty("patient.email");
    private static final String PATIENT_AGE     = PROPS.getProperty("patient.age");
    private static final String PATIENT_GENDER  = PROPS.getProperty("patient.gender");

    // -- Token captured in STEP 5, used in STEP 9 -----------------------------
    private static String generatedTokenNumber;

    // -- WebDriver and explicit wait ------------------------------------------
    private WebDriver     driver;
    private WebDriverWait wait;


    // ==========================================================================
    //  LIFECYCLE
    // ==========================================================================

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();

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
    // STEP 1 -- Admin seeds a Department and a Doctor (test setup)
    // --------------------------------------------------------------------------
    @Test(priority = 1,
          description = "Admin creates a department and a doctor so the patient has someone to pick")
    public void TC01_AdminSeedDepartmentAndDoctor() {

        new LoginPage(driver, wait).open(BASE_URL).loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);

        AdminLayoutPage admin = new AdminLayoutPage(driver, wait);
        admin.waitForAdminUrl();

        // Create Department
        DepartmentsPage departments = admin.gotoDepartments().waitUntilLoaded();
        departments.createDepartment(DEPT_NAME, DEPT_DESCRIPTION);
        departments.waitForSuccessAlert();
        departments.waitForRowContaining(DEPT_NAME);
        Assert.assertTrue(driver.getPageSource().contains(DEPT_NAME),
            "Department '" + DEPT_NAME + "' should appear in the departments table");

        // Create Doctor under that Department
        DoctorsPage doctors = new AdminLayoutPage(driver, wait).gotoDoctors().waitUntilLoaded();
        doctors.createDoctor(DOCTOR_NAME, DOCTOR_EMAIL, DOCTOR_PASSWORD,
                             DEPT_NAME, DOCTOR_SPECIALIZATION);
        doctors.waitForSuccessAlert();
        doctors.waitForRowContaining(DOCTOR_NAME);
        Assert.assertTrue(driver.getPageSource().contains(DOCTOR_NAME),
            "Doctor '" + DOCTOR_NAME + "' should appear in the doctors table");
    }


    // --------------------------------------------------------------------------
    // STEP 2 -- Admin logs out (patient flow runs unauthenticated)
    // --------------------------------------------------------------------------
    @Test(priority = 2,
          description = "Admin logs out before the patient self-service flow begins")
    public void TC02_AdminLogout() {

        LoginPage login = new AdminLayoutPage(driver, wait).logout();

        Assert.assertTrue(driver.getCurrentUrl().contains("/auth/login"),
            "Browser should be on /auth/login after admin logout");
        login.emailField();
    }


    // --------------------------------------------------------------------------
    // STEP 3 -- Patient lands on the public /patient home page
    // --------------------------------------------------------------------------
    @Test(priority = 3,
          description = "Patient opens the public /patient home page and sees the entry-point links")
    public void TC03_PatientLandsOnPublicHome() {

        PatientHomePage home = new PatientHomePage(driver, wait).open(BASE_URL);

        // Assertion 1 -- "Smart Hospital" heading must be visible
        WebElement heading = home.waitForHeading();
        Assert.assertTrue(heading.isDisplayed(),
            "'Smart Hospital' heading should be visible on /patient");

        // Assertion 2 -- URL is the public /patient route (no auth required)
        Assert.assertTrue(driver.getCurrentUrl().contains(PatientHomePage.PATH),
            "URL should be the public /patient landing page");
    }


    // --------------------------------------------------------------------------
    // STEP 4 -- Patient self-registers and provides personal details
    // --------------------------------------------------------------------------
    @Test(priority = 4,
          description = "Patient fills in their own details on /patient/register and advances to doctor selection")
    public void TC04_PatientSelfRegister() {

        PatientSelfRegisterPage register =
            new PatientHomePage(driver, wait).clickRegister();

        register.fillPatientDetails(PATIENT_NAME, PATIENT_PHONE, PATIENT_EMAIL,
                                    PATIENT_AGE, PATIENT_GENDER);

        // Assertion -- We are still on /patient/register (now on step 2)
        Assert.assertTrue(driver.getCurrentUrl().contains(PatientSelfRegisterPage.PATH),
            "Patient should remain on /patient/register after step 1");
    }


    // --------------------------------------------------------------------------
    // STEP 5 -- Patient picks Department + Doctor and gets a Token
    // --------------------------------------------------------------------------
    @Test(priority = 5,
          description = "Patient picks the seeded department and doctor and receives a token number")
    public void TC05_PatientPicksDoctorAndGetsToken() {

        PatientSelfRegisterPage register = new PatientSelfRegisterPage(driver, wait);

        register.pickDoctorAndGetToken(DEPT_NAME,
            DOCTOR_NAME + " — " + DOCTOR_SPECIALIZATION);

        // Assertion 1 -- Token display element appears
        WebElement tokenDisplay = register.waitForTokenDisplay();
        Assert.assertTrue(tokenDisplay.isDisplayed(),
            "Generated token number should be displayed on screen");

        // Assertion 2 -- Captured token number is not empty
        generatedTokenNumber = register.getTokenNumber();
        Assert.assertFalse(generatedTokenNumber.isEmpty(),
            "Captured token number must not be empty");

        // Assertion 3 -- Track-queue-position link is visible
        WebElement trackLink = register.waitForTrackLink();
        Assert.assertTrue(trackLink.isDisplayed(),
            "'Track My Queue Position' link should be visible after token generation");
    }


    // --------------------------------------------------------------------------
    // STEP 6 -- Doctor logs in and sees the patient in the queue
    // --------------------------------------------------------------------------
    @Test(priority = 6,
          description = "Doctor logs in and sees the self-registered patient waiting in the queue")
    public void TC06_DoctorLoginAndSeeQueue() {

        new LoginPage(driver, wait).open(BASE_URL).loginAs(DOCTOR_EMAIL, DOCTOR_PASSWORD);

        DoctorLayoutPage layout = new DoctorLayoutPage(driver, wait);
        layout.waitForDoctorUrl();

        // Assertion 1 -- Doctor navbar brand is visible
        WebElement brand = layout.waitForBrand();
        Assert.assertTrue(brand.isDisplayed(),
            "Doctor navbar brand should be visible after login");

        // Assertion 2 -- "My Queue" heading is visible
        DoctorQueuePage queue = new DoctorQueuePage(driver, wait);
        WebElement heading = queue.waitForHeading();
        Assert.assertTrue(heading.isDisplayed(),
            "'My Queue' heading should be visible on the doctor queue page");

        // Assertion 3 -- The self-registered patient appears in the waiting queue
        queue.waitForPatientRow(PATIENT_NAME);
        Assert.assertTrue(driver.getPageSource().contains(PATIENT_NAME),
            "'" + PATIENT_NAME + "' should appear in the doctor's waiting queue");
    }


    // --------------------------------------------------------------------------
    // STEP 7 -- Doctor calls the patient
    // --------------------------------------------------------------------------
    @Test(priority = 7,
          description = "Doctor clicks 'Call First Patient' and the patient becomes IN_PROGRESS")
    public void TC07_DoctorCallPatient() {

        DoctorQueuePage queue = new DoctorQueuePage(driver, wait);
        queue.callFirstPatient();

        // Assertion 1 -- Token number appears in the Current Patient card
        WebElement tokenDisplay = queue.waitForTokenDisplay();
        Assert.assertTrue(tokenDisplay.isDisplayed(),
            "Token number should appear in the Current Patient card after calling");

        // Assertion 2 -- Patient name is shown as the current patient
        Assert.assertTrue(driver.getPageSource().contains(PATIENT_NAME),
            "'" + PATIENT_NAME + "' should be shown as the current patient");

        // Assertion 3 -- "Complete & Done" button is visible
        WebElement completeBtn = queue.waitForCompleteButton();
        Assert.assertTrue(completeBtn.isDisplayed(),
            "'Complete & Done' button should be visible when the patient is IN_PROGRESS");
    }


    // --------------------------------------------------------------------------
    // STEP 8 -- Doctor completes the consultation
    // --------------------------------------------------------------------------
    @Test(priority = 8,
          description = "Doctor clicks 'Complete & Done' -- token becomes COMPLETED and queue is empty")
    public void TC08_DoctorCompleteConsultation() {

        DoctorQueuePage queue = new DoctorQueuePage(driver, wait);
        queue.completeCurrentToken();

        // Assertion 1 -- Empty current-patient message appears
        WebElement noPatientMsg = queue.waitForNoCurrentPatientMessage();
        Assert.assertTrue(noPatientMsg.isDisplayed(),
            "'No patient is currently in consultation' should appear after completing");

        // Assertion 2 -- Empty waiting queue message appears
        WebElement emptyQueueMsg = queue.waitForNoWaitingQueueMessage();
        Assert.assertTrue(emptyQueueMsg.isDisplayed(),
            "'No patients waiting in the queue' should appear after the only patient is done");
    }


    // --------------------------------------------------------------------------
    // STEP 9 -- Patient tracks the token and sees "Consultation Completed"
    // --------------------------------------------------------------------------
    @Test(priority = 9,
          description = "Patient enters the token number on the public tracker and sees 'Consultation Completed'")
    public void TC09_PatientTrackTokenCompleted() {

        Assert.assertNotNull(generatedTokenNumber,
            "TC09 cannot run -- TC05 did not capture a token number (self-registration failed)");

        TrackTokenPage track = new TrackTokenPage(driver, wait).open(BASE_URL);
        track.trackToken(generatedTokenNumber);

        // Assertion 1 -- "Consultation Completed" banner is visible
        WebElement completedMsg = track.waitForCompletedMessage();
        Assert.assertTrue(completedMsg.isDisplayed(),
            "'Consultation Completed' should appear when the token status is COMPLETED");

        // Assertion 2 -- "Thank you for visiting" message is visible
        WebElement thankYouMsg = track.waitForThankYouMessage();
        Assert.assertTrue(thankYouMsg.isDisplayed(),
            "'Thank you for visiting' should appear below the completed banner");
    }
}
