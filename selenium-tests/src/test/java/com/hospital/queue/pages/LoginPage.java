package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the public Login screen at /auth/login.
 * Uses Selenium PageFactory to lazily initialise the WebElements
 * declared with @FindBy.
 */
public class LoginPage extends BasePage {

    public static final String PATH = "/auth/login";

    @FindBy(how = How.XPATH, using = "//input[@type='email']")
    private WebElement emailField;

    @FindBy(how = How.XPATH, using = "//input[@type='password']")
    private WebElement passwordField;

    @FindBy(how = How.XPATH, using = "//button[@type='submit']")
    private WebElement signInButton;

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
        PageFactory.initElements(driver, this);
    }

    /** Navigates the browser to /auth/login and waits for the email field. */
    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + PATH);
        waitVisible(emailField);
        return this;
    }

    /** Fills the login form and clicks Sign In. */
    public void loginAs(String email, String password) {
        typeInto(emailField, email);
        typeInto(passwordField, password);
        clickWhenReady(signInButton);
    }

    public WebElement emailField() {
        return waitVisible(emailField);
    }
}
