package com.hospital.queue.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Admin Doctors page at /admin/doctors. */
public class DoctorsPage extends BasePage {

    private static final By HEADING        = By.xpath("//h2[text()='Doctors']");
    private static final By NAME_INPUT     = By.xpath("//input[@placeholder='Dr. Jane Smith']");
    private static final By EMAIL_INPUT    = By.xpath("//input[@type='email']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@type='password']");
    private static final By DEPT_SELECT    = By.xpath("(//select)[1]");
    private static final By SPEC_INPUT     = By.xpath("//input[@placeholder='e.g., Cardiologist']");
    private static final By CREATE_BUTTON  = By.xpath("//button[contains(text(),'Create Doctor')]");
    private static final By SUCCESS_ALERT  = By.className("alert-success");

    public DoctorsPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public DoctorsPage waitUntilLoaded() {
        waitVisible(HEADING);
        return this;
    }

    public void createDoctor(String fullName,
                             String email,
                             String password,
                             String departmentVisibleText,
                             String specialization) {
        typeInto(NAME_INPUT, fullName);
        typeInto(EMAIL_INPUT, email);
        typeInto(PASSWORD_INPUT, password);
        selectFromPopulatedDropdown(DEPT_SELECT, departmentVisibleText);
        typeInto(SPEC_INPUT, specialization);
        clickWhenReady(CREATE_BUTTON);
    }

    public WebElement waitForSuccessAlert() {
        return waitVisible(SUCCESS_ALERT);
    }

    public WebElement waitForRowContaining(String text) {
        return waitVisible(By.xpath("//table//td[contains(text(),'" + text + "')]"));
    }
}
