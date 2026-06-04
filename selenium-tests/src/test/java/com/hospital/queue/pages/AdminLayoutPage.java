package com.hospital.queue.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object for the navbar / layout shared by every /admin/* page. */
public class AdminLayoutPage extends BasePage {

    private static final By BRAND_ADMIN       = By.xpath("//span[@class='brand' and contains(text(),'Admin')]");
    private static final By DEPARTMENTS_LINK  = By.xpath("//nav/a[contains(text(),'Departments')]");
    private static final By DOCTORS_LINK      = By.xpath("//nav/a[contains(text(),'Doctors')]");
    private static final By USERS_LINK        = By.xpath("//nav/a[contains(text(),'Users')]");
    private static final By LOGOUT_BUTTON     = By.className("logout-btn");

    public AdminLayoutPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void waitForAdminUrl() {
        waitForUrl("/admin");
    }

    public WebElement waitForBrand() {
        return waitVisible(BRAND_ADMIN);
    }

    public DepartmentsPage gotoDepartments() {
        clickWhenReady(DEPARTMENTS_LINK);
        waitForUrl("/admin/departments");
        return new DepartmentsPage(driver, wait);
    }

    public DoctorsPage gotoDoctors() {
        clickWhenReady(DOCTORS_LINK);
        waitForUrl("/admin/doctors");
        return new DoctorsPage(driver, wait);
    }

    public UsersPage gotoUsers() {
        clickWhenReady(USERS_LINK);
        waitForUrl("/admin/users");
        return new UsersPage(driver, wait);
    }

    public LoginPage logout() {
        clickWhenReady(LOGOUT_BUTTON);
        waitForUrl(LoginPage.PATH);
        return new LoginPage(driver, wait);
    }
}
