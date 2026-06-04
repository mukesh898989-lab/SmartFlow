package com.hospital.queue.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Admin Dashboard ("Queue Overview") at /admin. */
public class AdminDashboardPage extends BasePage {

    private static final By HEADING = By.xpath("//h2[contains(text(),'Queue Overview')]");

    public AdminDashboardPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public WebElement waitForHeading() {
        return waitVisible(HEADING);
    }
}
