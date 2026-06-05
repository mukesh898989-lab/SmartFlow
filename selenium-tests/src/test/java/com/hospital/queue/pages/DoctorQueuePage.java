package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Doctor Queue page at /doctor/queue. */
public class DoctorQueuePage extends BasePage {

    private static final By HEADING                = By.xpath("//h2[contains(text(),'My Queue')]");
    private static final By CALL_FIRST_BUTTON      = By.xpath("//button[contains(text(),'Call First Patient')]");
    private static final By COMPLETE_BUTTON        = By.xpath("//button[contains(text(),'Complete')]");
    private static final By TOKEN_DISPLAY          = By.className("token-number");
    private static final By NO_CURRENT_PATIENT_MSG = By.xpath("//*[contains(text(),'No patient is currently in consultation')]");
    private static final By NO_WAITING_QUEUE_MSG   = By.xpath("//*[contains(text(),'No patients waiting in the queue')]");

    public DoctorQueuePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public WebElement waitForHeading() {
        return waitVisible(HEADING);
    }

    public WebElement waitForPatientRow(String patientName) {
        return waitVisible(By.xpath("//table//td[contains(text(),'" + patientName + "')]"));
    }

    public void callFirstPatient() {
        waitClickable(CALL_FIRST_BUTTON).click();
    }

    public WebElement waitForTokenDisplay() {
        return waitVisible(TOKEN_DISPLAY);
    }

    public WebElement waitForCompleteButton() {
        return waitVisible(COMPLETE_BUTTON);
    }

    public void completeCurrentToken() {
        clickWhenReady(COMPLETE_BUTTON);
    }

    public WebElement waitForNoCurrentPatientMessage() {
        return waitVisible(NO_CURRENT_PATIENT_MSG);
    }

    public WebElement waitForNoWaitingQueueMessage() {
        return waitVisible(NO_WAITING_QUEUE_MSG);
    }
}
