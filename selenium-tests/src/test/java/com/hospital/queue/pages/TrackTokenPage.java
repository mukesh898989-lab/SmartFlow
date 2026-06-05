package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the public Patient Track Token page at /patient/track. */
public class TrackTokenPage extends BasePage {

    public static final String PATH = "/patient/track";

    private static final By HEADING       = By.xpath("//h2[contains(text(),'Track Your Token')]");
    private static final By TOKEN_INPUT   = By.xpath("//input[contains(@placeholder,'token') or contains(@placeholder,'Token')]");
    private static final By TRACK_BUTTON  = By.xpath("//button[contains(text(),'Track')]");
    private static final By COMPLETED_MSG = By.xpath("//*[contains(text(),'Consultation Completed')]");
    private static final By THANK_YOU_MSG = By.xpath("//*[contains(text(),'Thank you for visiting')]");

    public TrackTokenPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public TrackTokenPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        waitForUrl(PATH);
        waitVisible(HEADING);
        return this;
    }

    public void trackToken(String tokenNumber) {
        WebElement input = waitVisible(TOKEN_INPUT);
        input.clear();
        input.sendKeys(tokenNumber);
        clickWhenReady(TRACK_BUTTON);
    }

    public WebElement waitForCompletedMessage() {
        return waitVisible(COMPLETED_MSG);
    }

    public WebElement waitForThankYouMessage() {
        return waitVisible(THANK_YOU_MSG);
    }
}
