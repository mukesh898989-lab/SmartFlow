package com.hospital.queue.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared Selenium helpers for every Page Object.
 * All concrete pages extend this class so they share the same
 * wait / type / select / click primitives.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void typeInto(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void typeInto(WebElement element, String text) {
        WebElement el = waitVisible(element);
        el.clear();
        el.sendKeys(text);
    }

    protected void selectByText(By locator, String visibleText) {
        WebElement el = waitVisible(locator);
        new Select(el).selectByVisibleText(visibleText);
    }

    /** Waits for the dropdown to load more than just its placeholder option, then selects. */
    protected void selectFromPopulatedDropdown(By locator, String visibleText) {
        WebElement el = waitVisible(locator);
        wait.until(d -> new Select(el).getOptions().size() > 1);
        new Select(el).selectByVisibleText(visibleText);
    }

    protected void clickWhenReady(By locator) {
        waitClickable(locator).click();
    }

    protected void clickWhenReady(WebElement element) {
        waitClickable(element).click();
    }

    protected void waitForUrl(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }
}
