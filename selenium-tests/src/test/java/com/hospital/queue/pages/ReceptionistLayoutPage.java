package com.hospital.queue.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the navbar / layout shared by every /receptionist/* page. */
public class ReceptionistLayoutPage extends BasePage {

    private static final By BRAND_RECEPT         = By.xpath("//span[@class='brand' and contains(text(),'Receptionist')]");
    private static final By REGISTER_PATIENT_LINK = By.xpath("//nav/a[contains(text(),'Register Patient')]");
    private static final By GENERATE_TOKEN_LINK  = By.xpath("//nav/a[contains(text(),'Generate Token')]");
    private static final By LOGOUT_BUTTON        = By.className("logout-btn");

    public ReceptionistLayoutPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void waitForReceptionistUrl() {
        waitForUrl("/receptionist");
    }

    public WebElement waitForBrand() {
        return waitVisible(BRAND_RECEPT);
    }

    public WebElement waitForRegisterPatientLink() {
        return waitVisible(REGISTER_PATIENT_LINK);
    }

    public WebElement waitForGenerateTokenLink() {
        return waitVisible(GENERATE_TOKEN_LINK);
    }

    public RegisterPatientPage gotoRegisterPatient() {
        clickWhenReady(REGISTER_PATIENT_LINK);
        waitForUrl("/receptionist/register");
        return new RegisterPatientPage(driver, wait);
    }

    public LoginPage logout() {
        clickWhenReady(LOGOUT_BUTTON);
        waitForUrl(LoginPage.PATH);
        return new LoginPage(driver, wait);
    }
}
