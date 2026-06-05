package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the public Patient landing page at /patient. */
public class PatientHomePage extends BasePage {

    public static final String PATH = "/patient";

    private static final By HEADING        = By.xpath("//h1[contains(text(),'Smart Hospital')]");
    private static final By REGISTER_LINK  = By.xpath("//a[contains(.,'Register') and contains(.,'Get Token')]");
    private static final By TRACK_LINK     = By.xpath("//a[contains(.,'Track Your Token')]");

    public PatientHomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public PatientHomePage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        waitVisible(HEADING);
        return this;
    }

    public WebElement waitForHeading() {
        return waitVisible(HEADING);
    }

    public PatientSelfRegisterPage clickRegister() {
        clickWhenReady(REGISTER_LINK);
        waitForUrl(PatientSelfRegisterPage.PATH);
        return new PatientSelfRegisterPage(driver, wait).waitUntilLoaded();
    }

    public TrackTokenPage clickTrack() {
        clickWhenReady(TRACK_LINK);
        waitForUrl(TrackTokenPage.PATH);
        return new TrackTokenPage(driver, wait);
    }
}
