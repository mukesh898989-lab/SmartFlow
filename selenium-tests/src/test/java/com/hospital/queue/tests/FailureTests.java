package com.hospital.queue.tests;

import com.hospital.queue.pages.LoginPage;
import com.hospital.queue.pages.TrackTokenPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
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
 *  SmartFlowApp -- NEGATIVE / FAILURE Test Suite (DEMO)
 * ============================================================
 *
 * This suite contains 3 short tests that are EXPECTED TO FAIL.
 * They exist to demonstrate that the automation framework correctly
 * detects and reports defects (for evaluator review).
 *
 *   TC_FAIL_01 -- Admin login with WRONG password
 *                 (assertion expects /admin URL -> fails because
 *                  backend rejects credentials and URL stays at /auth/login)
 *
 *   TC_FAIL_02 -- Click Sign In with EMPTY credentials
 *                 (assertion expects /admin URL -> fails because
 *                  Angular form validation blocks submission)
 *
 *   TC_FAIL_03 -- Track a NON-EXISTENT token
 *                 (assertion expects 'Consultation Completed' banner
 *                  -> fails because no such token exists in the DB)
 *
 * Driver lifecycle:
 *   Each individual test has a 2.5-second pause BEFORE the assertion
 *   so the failure screen is visible to the evaluator while the suite
 *   is running. After all 3 tests finish, @AfterClass waits a few
 *   extra seconds so the final failure screen can be inspected, then
 *   cleanly quits the driver.
 *
 * Prerequisites:
 *   1. Backend running at http://localhost:8081
 *   2. Frontend running at http://localhost:4200
 *   3. Google Chrome installed
 *
 * Run: cd selenium-tests && mvn test -Dtest=FailureTests
 */
public class FailureTests {

    private static final Properties PROPS = loadProps();

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = FailureTests.class.getClassLoader()
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

    private static final String BASE_URL    = PROPS.getProperty("app.base.url");
    private static final String ADMIN_EMAIL = PROPS.getProperty("admin.email");

    // Shorter wait so TC_FAIL_03 fails fast (~5s) instead of dragging 20s
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(5);

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

    /**
     * Pauses briefly so the final failure screen is still visible,
     * then cleanly quits the driver and closes the Chrome window.
     */
    @AfterClass
    public void tearDown() {
        System.out.println("\n========================================================");
        System.out.println(" FAILURE-SUITE COMPLETE -- closing browser in 5s...");
        System.out.println("========================================================\n");
        pause(5000);
        if (driver != null) {
            driver.quit();
        }
    }

    /** Tiny pause so the UI is visually settled before the assertion fires. */
    private static void pause(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }


    // ==========================================================================
    //  TEST STEPS (all expected to FAIL)
    // ==========================================================================

    // --------------------------------------------------------------------------
    // TC_FAIL_01 -- Invalid Admin Login
    // --------------------------------------------------------------------------
    @Test(priority = 1,
          description = "FAIL: Admin login with wrong password -- should NOT reach /admin")
    public void TC_FAIL_01_InvalidAdminLogin() {

        new LoginPage(driver, wait).open(BASE_URL).loginAs(ADMIN_EMAIL, "WrongPassword@123");

        pause(2500); // let the error toast / red border render so the evaluator can see it

        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"),
            "EXPECTED admin dashboard after login but URL was: " + driver.getCurrentUrl());
    }


    // --------------------------------------------------------------------------
    // TC_FAIL_02 -- Empty Credentials Submit
    // --------------------------------------------------------------------------
    @Test(priority = 2,
          description = "FAIL: Click Sign In with empty credentials -- should NOT reach /admin")
    public void TC_FAIL_02_EmptyCredentialsLogin() {

        LoginPage login = new LoginPage(driver, wait).open(BASE_URL);
        // Do NOT type anything -- click Sign In with empty fields
        login.loginAs("", "");

        pause(2500); // let the validation messages render so the evaluator can see them

        Assert.assertTrue(driver.getCurrentUrl().contains("/admin"),
            "EXPECTED admin dashboard after submit but URL was: " + driver.getCurrentUrl());
    }


    // --------------------------------------------------------------------------
    // TC_FAIL_03 -- Track a Non-Existent Token
    // --------------------------------------------------------------------------
    @Test(priority = 3,
          description = "FAIL: Track a non-existent token -- 'Consultation Completed' should NOT appear")
    public void TC_FAIL_03_TrackInvalidToken() {

        TrackTokenPage track = new TrackTokenPage(driver, wait).open(BASE_URL);
        track.trackToken("INVALID-TOKEN-9999");

        pause(2500); // let the 'token not found' error render so the evaluator can see it

        // This will throw TimeoutException (~5s) because the banner never appears.
        // TestNG marks that as a FAILED test, which is exactly what we want to demo.
        Assert.assertTrue(track.waitForCompletedMessage().isDisplayed(),
            "EXPECTED 'Consultation Completed' banner for a non-existent token (it should never appear)");
    }
}
