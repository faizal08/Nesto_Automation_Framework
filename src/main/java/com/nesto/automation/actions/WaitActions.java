package com.nesto.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class WaitActions {
    private WebDriverWait wait;
    private WebDriver driver;

    public WaitActions(WebDriver driver) {
        this.driver = driver;
        // Set a global 10-second timeout
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public WebElement waitForElementVisible(String xpath) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    public WebElement waitForElementClickable(String xpath) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    }

    /**
     * UPDATED: Now returns a boolean to satisfy the 'VerificationActions' class.
     * If the URL doesn't match within 10 seconds, this will throw a TimeoutException.
     */
    public boolean waitForUrl(String expectedUrlPart) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // This is the key: it waits until the address bar actually changes
        return wait.until(ExpectedConditions.urlContains(expectedUrlPart));
    }
}