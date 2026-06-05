package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Receptionist Register Patient page at /receptionist/register. */
public class RegisterPatientPage extends BasePage {

    private static final By HEADING                 = By.xpath("//h2[contains(text(),'Register Patient')]");
    private static final By NAME_INPUT              = By.xpath("//input[@placeholder='Patient full name']");
    private static final By PHONE_INPUT             = By.xpath("//input[@placeholder='10-15 digit phone number']");
    private static final By EMAIL_INPUT             = By.xpath("//input[@type='email']");
    private static final By AGE_INPUT               = By.xpath("//input[@type='number']");
    private static final By GENDER_SELECT           = By.xpath("(//select)[1]");
    private static final By SUBMIT_BUTTON           = By.xpath("//button[contains(text(),'Register Patient')]");
    private static final By SUCCESS_ALERT           = By.className("alert-success");
    private static final By GENERATE_TOKEN_QUICK_LINK =
            By.xpath("//a[contains(text(),'Generate Token for this patient')]");

    public RegisterPatientPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public RegisterPatientPage waitUntilLoaded() {
        waitVisible(HEADING);
        return this;
    }

    public void registerPatient(String fullName,
                                String phone,
                                String email,
                                String age,
                                String gender) {
        typeInto(NAME_INPUT, fullName);
        typeInto(PHONE_INPUT, phone);
        typeInto(EMAIL_INPUT, email);
        typeInto(AGE_INPUT, age);
        selectByText(GENDER_SELECT, gender);
        clickWhenReady(SUBMIT_BUTTON);
    }

    public WebElement waitForSuccessAlert() {
        return waitVisible(SUCCESS_ALERT);
    }

    public WebElement waitForGenerateTokenQuickLink() {
        return waitVisible(GENERATE_TOKEN_QUICK_LINK);
    }

    public GenerateTokenPage clickGenerateTokenQuickLink() {
        clickWhenReady(GENERATE_TOKEN_QUICK_LINK);
        waitForUrl("/receptionist/generate-token");
        return new GenerateTokenPage(driver, wait);
    }
}
