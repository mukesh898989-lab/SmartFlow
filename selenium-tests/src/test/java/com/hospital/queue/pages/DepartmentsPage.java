package com.hospital.queue.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Admin Departments page at /admin/departments. */
public class DepartmentsPage extends BasePage {

    private static final By HEADING        = By.xpath("//h2[text()='Departments']");
    private static final By NAME_INPUT     = By.xpath("//input[@placeholder='e.g., Cardiology']");
    private static final By DESC_INPUT     = By.xpath("//input[@placeholder='Brief description']");
    private static final By CREATE_BUTTON  = By.xpath("//button[contains(text(),'Create Department')]");
    private static final By SUCCESS_ALERT  = By.className("alert-success");

    public DepartmentsPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public DepartmentsPage waitUntilLoaded() {
        waitVisible(HEADING);
        return this;
    }

    public void createDepartment(String name, String description) {
        typeInto(NAME_INPUT, name);
        typeInto(DESC_INPUT, description);
        clickWhenReady(CREATE_BUTTON);
    }

    public WebElement waitForSuccessAlert() {
        return waitVisible(SUCCESS_ALERT);
    }

    public WebElement waitForRowContaining(String text) {
        return waitVisible(By.xpath("//table//td[contains(text(),'" + text + "')]"));
    }
}
