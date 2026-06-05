package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the navbar / layout shared by every /doctor/* page. */
public class DoctorLayoutPage extends BasePage {

    public static final String QUEUE_PATH = "/doctor/queue";

    private static final By BRAND_DOCTOR  = By.xpath("//span[@class='brand' and contains(text(),'Doctor')]");
    private static final By LOGOUT_BUTTON = By.className("logout-btn");

    public DoctorLayoutPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void waitForDoctorUrl() {
        waitForUrl("/doctor");
    }

    public WebElement waitForBrand() {
        return waitVisible(BRAND_DOCTOR);
    }

    public DoctorQueuePage openQueue(String baseUrl) {
        driver.get(baseUrl + QUEUE_PATH);
        waitForUrl("/doctor");
        return new DoctorQueuePage(driver, wait);
    }

    public LoginPage logout() {
        clickWhenReady(LOGOUT_BUTTON);
        waitForUrl(LoginPage.PATH);
        return new LoginPage(driver, wait);
    }
}
