package com.hospital.queue.pages;

import com.hospital.queue.utils.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the navbar / layout shared by every /admin/* page.
 * Uses Selenium PageFactory to lazily initialise the WebElements
 * declared with @FindBy.
 */
public class AdminLayoutPage extends BasePage {

    @FindBy(how = How.XPATH, using = "//span[@class='brand' and contains(text(),'Admin')]")
    private WebElement brandAdmin;

    @FindBy(how = How.XPATH, using = "//nav/a[contains(text(),'Departments')]")
    private WebElement departmentsLink;

    @FindBy(how = How.XPATH, using = "//nav/a[contains(text(),'Doctors')]")
    private WebElement doctorsLink;

    @FindBy(how = How.XPATH, using = "//nav/a[contains(text(),'Users')]")
    private WebElement usersLink;

    @FindBy(how = How.CLASS_NAME, using = "logout-btn")
    private WebElement logoutButton;

    public AdminLayoutPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
        PageFactory.initElements(driver, this);
    }

    public void waitForAdminUrl() {
        waitForUrl("/admin");
    }

    public WebElement waitForBrand() {
        return waitVisible(brandAdmin);
    }

    public DepartmentsPage gotoDepartments() {
        clickWhenReady(departmentsLink);
        waitForUrl("/admin/departments");
        return new DepartmentsPage(driver, wait);
    }

    public DoctorsPage gotoDoctors() {
        clickWhenReady(doctorsLink);
        waitForUrl("/admin/doctors");
        return new DoctorsPage(driver, wait);
    }

    public UsersPage gotoUsers() {
        clickWhenReady(usersLink);
        waitForUrl("/admin/users");
        return new UsersPage(driver, wait);
    }

    public LoginPage logout() {
        clickWhenReady(logoutButton);
        waitForUrl(LoginPage.PATH);
        return new LoginPage(driver, wait);
    }
}
