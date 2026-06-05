package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Receptionist Generate Token page at /receptionist/generate-token. */
public class GenerateTokenPage extends BasePage {

    private static final By HEADING         = By.xpath("//h2[contains(text(),'Generate Token')]");
    private static final By PATIENT_SELECT  = By.xpath("(//select)[1]");
    private static final By DEPT_SELECT     = By.xpath("(//select)[2]");
    private static final By DOCTOR_SELECT   = By.xpath("(//select)[3]");
    private static final By PRIORITY_SELECT = By.xpath("(//select)[4]");
    private static final By SUBMIT_BUTTON   = By.xpath("//button[contains(text(),'Generate Token')]");
    private static final By TOKEN_DISPLAY   = By.className("token-number");

    public GenerateTokenPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public GenerateTokenPage waitUntilLoaded() {
        waitVisible(HEADING);
        return this;
    }

    public void selectPatient(String visibleText) {
        selectFromPopulatedDropdown(PATIENT_SELECT, visibleText);
    }

    public void selectDepartment(String visibleText) {
        selectFromPopulatedDropdown(DEPT_SELECT, visibleText);
    }

    public void selectDoctor(String visibleText) {
        selectFromPopulatedDropdown(DOCTOR_SELECT, visibleText);
    }

    public void selectPriority(String visibleText) {
        selectByText(PRIORITY_SELECT, visibleText);
    }

    public void submit() {
        clickWhenReady(SUBMIT_BUTTON);
    }

    public WebElement waitForTokenDisplay() {
        return waitVisible(TOKEN_DISPLAY);
    }

    public String getTokenNumber() {
        return waitForTokenDisplay().getText().trim();
    }
}
