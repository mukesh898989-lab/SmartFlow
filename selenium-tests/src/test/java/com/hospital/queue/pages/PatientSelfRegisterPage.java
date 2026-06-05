package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the public Patient self-register / get-token flow at
 * /patient/register. This is the three-step component:
 *   Step 1 - Patient enters their own details and clicks "Continue"
 *   Step 2 - Patient picks a department + doctor and clicks "Get My Token"
 *   Step 3 - Generated token number is shown on screen
 */
public class PatientSelfRegisterPage extends BasePage {

    public static final String PATH = "/patient/register";

    private static final By HEADING        = By.xpath("//h2[contains(text(),'Get Queue Token')]");

    private static final By NAME_INPUT     = By.xpath("//input[@formcontrolname='name']");
    private static final By PHONE_INPUT    = By.xpath("//input[@formcontrolname='phone']");
    private static final By EMAIL_INPUT    = By.xpath("//input[@formcontrolname='email']");
    private static final By AGE_INPUT      = By.xpath("//input[@formcontrolname='age']");
    private static final By GENDER_SELECT  = By.xpath("//select[@formcontrolname='gender']");
    private static final By CONTINUE_BTN   = By.xpath("//button[contains(text(),'Continue')]");

    private static final By DEPT_SELECT    = By.xpath("//select[@formcontrolname='departmentId']");
    private static final By DOCTOR_SELECT  = By.xpath("//select[@formcontrolname='doctorId']");
    private static final By GET_TOKEN_BTN  = By.xpath("//button[contains(text(),'Get My Token')]");

    private static final By TOKEN_DISPLAY  = By.className("token-number");
    private static final By TRACK_LINK     = By.xpath("//a[contains(text(),'Track My Queue Position')]");

    public PatientSelfRegisterPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public PatientSelfRegisterPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        waitVisible(HEADING);
        return this;
    }

    public PatientSelfRegisterPage waitUntilLoaded() {
        waitVisible(HEADING);
        return this;
    }

    /** Step 1 - fill in personal details and click Continue. */
    public void fillPatientDetails(String fullName,
                                   String phone,
                                   String email,
                                   String age,
                                   String gender) {
        typeInto(NAME_INPUT, fullName);
        typeInto(PHONE_INPUT, phone);
        typeInto(EMAIL_INPUT, email);
        typeInto(AGE_INPUT, age);
        selectByText(GENDER_SELECT, gender);
        clickWhenReady(CONTINUE_BTN);
    }

    /** Step 2 - select department and doctor, then click Get My Token. */
    public void pickDoctorAndGetToken(String departmentName, String doctorOptionText) {
        selectFromPopulatedDropdown(DEPT_SELECT, departmentName);
        selectFromPopulatedDropdown(DOCTOR_SELECT, doctorOptionText);
        clickWhenReady(GET_TOKEN_BTN);
    }

    /** Step 3 - waits for and returns the generated token-number element. */
    public WebElement waitForTokenDisplay() {
        return waitVisible(TOKEN_DISPLAY);
    }

    public String getTokenNumber() {
        return waitForTokenDisplay().getText().trim();
    }

    public WebElement waitForTrackLink() {
        return waitVisible(TRACK_LINK);
    }
}
