package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the Admin Staff Users page at /admin/users. */
public class UsersPage extends BasePage {

    private static final By HEADING        = By.xpath("//h2[text()='Staff Users']");
    private static final By NAME_INPUT     = By.xpath("//input[@placeholder='Staff Name']");
    private static final By EMAIL_INPUT    = By.xpath("//input[@type='email']");
    private static final By PASSWORD_INPUT = By.xpath("//input[@type='password']");
    private static final By ROLE_SELECT    = By.xpath("(//select)[1]");
    private static final By CREATE_BUTTON  = By.xpath("//button[contains(text(),'Create User')]");
    private static final By SUCCESS_ALERT  = By.className("alert-success");

    public UsersPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public UsersPage waitUntilLoaded() {
        waitVisible(HEADING);
        return this;
    }

    public void createUser(String fullName, String email, String password, String role) {
        typeInto(NAME_INPUT, fullName);
        typeInto(EMAIL_INPUT, email);
        typeInto(PASSWORD_INPUT, password);
        selectByText(ROLE_SELECT, role);
        clickWhenReady(CREATE_BUTTON);
    }

    public WebElement waitForSuccessAlert() {
        return waitVisible(SUCCESS_ALERT);
    }

    public WebElement waitForRowContaining(String text) {
        return waitVisible(By.xpath("//table//td[contains(text(),'" + text + "')]"));
    }
}
