package com.hospital.queue.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the public Login screen at /auth/login. */
public class LoginPage extends BasePage {

    public static final String PATH = "/auth/login";

    private static final By EMAIL_FIELD     = By.xpath("//input[@type='email']");
    private static final By PASSWORD_FIELD  = By.xpath("//input[@type='password']");
    private static final By SIGN_IN_BUTTON  = By.xpath("//button[@type='submit']");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    /** Navigates the browser to /auth/login and waits for the email field. */
    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        waitVisible(EMAIL_FIELD);
        return this;
    }

    /** Fills the login form and clicks Sign In. */
    public void loginAs(String email, String password) {
        typeInto(EMAIL_FIELD, email);
        typeInto(PASSWORD_FIELD, password);
        clickWhenReady(SIGN_IN_BUTTON);
    }

    public WebElement emailField() {
        return waitVisible(EMAIL_FIELD);
    }
}
